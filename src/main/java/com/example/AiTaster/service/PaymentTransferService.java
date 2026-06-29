package com.example.AiTaster.service;

import com.example.AiTaster.constant.PaymentMethod;
import com.example.AiTaster.constant.PaymentStatus;
import com.example.AiTaster.constant.UserWalletStatus;
import com.example.AiTaster.dto.request.PaymentTransferRequest;
import com.example.AiTaster.entity.PaymentTransaction;
import com.example.AiTaster.entity.UserWallet;
import com.example.AiTaster.exception.GlobalException;
import com.example.AiTaster.repository.PaymentTransactionRepo;
import com.example.AiTaster.repository.UserWalletRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PaymentTransferService {
    private final UserWalletRepo userWalletRepo;
    private final PaymentTransactionRepo paymentTransactionRepo;
    private final PaymentCodeGenerator paymentCodeGenerator;

    @Transactional
    public PaymentTransaction transfer(PaymentTransferRequest request) {
        validateRequest(request);

        BigDecimal fromAmount = normalize(request.getFromAmount());
        BigDecimal receiveAmount = normalize(request.getReceiveAmount());
        BigDecimal amount = receiveAmount.compareTo(BigDecimal.ZERO) > 0 ? receiveAmount : fromAmount;

        UserWallet sourceWallet = null;
        UserWallet targetWallet = null;

        if (Boolean.TRUE.equals(request.getDebitSourceWallet())) {
            sourceWallet = lockWallet(request.getSourceWalletId(), "Source wallet not found");
            checkWalletActive(sourceWallet);
            if (sourceWallet.getBalance().compareTo(fromAmount) < 0) {
                throw new GlobalException(400, "Insufficient wallet balance");
            }
            sourceWallet.setBalance(sourceWallet.getBalance().subtract(fromAmount));
        }

        if (Boolean.TRUE.equals(request.getCreditTargetWallet())) {
            targetWallet = lockWallet(request.getTargetWalletId(), "Target wallet not found");
            checkWalletActive(targetWallet);
            targetWallet.setBalance(targetWallet.getBalance().add(receiveAmount));
        }

        if (sourceWallet != null) {
            userWalletRepo.save(sourceWallet);
        }
        if (targetWallet != null) {
            userWalletRepo.save(targetWallet);
        }

        PaymentTransaction transaction = PaymentTransaction.builder()
                .projectEscrowId(request.getProjectEscrowId())
                .expertServiceId(request.getExpertServiceId())
                .senderId(request.getSenderId())
                .receiverId(request.getReceiverId())
                .sourceWalletId(request.getSourceWalletId())
                .targetWalletId(request.getTargetWalletId())
                .amount(amount)
                .fromAmount(fromAmount)
                .receiveAmount(receiveAmount)
                .currency(request.getCurrency() == null ? "VND" : request.getCurrency())
                .transactionType(request.getTransactionType())
                .paymentMethod(request.getPaymentMethod() == null ? PaymentMethod.WALLET : request.getPaymentMethod())
                .paymentStatus(request.getPaymentStatus() == null ? PaymentStatus.SUCCESS : request.getPaymentStatus())
                .referenceId(request.getReferenceId())
                .paymentReferenceType(request.getPaymentReferenceType())
                .providerName(request.getProviderName() == null ? "INTERNAL" : request.getProviderName())
                .paymentCode(resolvePaymentCode(request))
                .providerTransactionCode(request.getProviderTransactionCode())
                .providerContent(request.getProviderContent())
                .description(request.getDescription())
                .paidAt(request.getPaidAt() == null ? LocalDateTime.now() : request.getPaidAt())
                .expiredAt(request.getExpiredAt())
                .build();

        return paymentTransactionRepo.save(transaction);
    }

    @Transactional
    public PaymentTransaction completeIncomingPayment(
            PaymentTransaction payment,
            String providerTransactionCode,
            String providerContent,
            LocalDateTime paidAt
    ) {
        if (payment == null) {
            throw new GlobalException(404, "Payment transaction not found");
        }
        if (!PaymentStatus.PENDING.equals(payment.getPaymentStatus())) {
            throw new GlobalException(400, "Payment is not pending");
        }
        if (payment.getTargetWalletId() != null) {
            UserWallet targetWallet = lockWallet(payment.getTargetWalletId(), "Target wallet not found");
            checkWalletActive(targetWallet);
            BigDecimal creditAmount = payment.getReceiveAmount() == null
                    ? payment.getAmount()
                    : payment.getReceiveAmount();
            targetWallet.setBalance(targetWallet.getBalance().add(creditAmount));
            userWalletRepo.save(targetWallet);
        }

        payment.setPaymentStatus(PaymentStatus.SUCCESS);
        payment.setProviderTransactionCode(providerTransactionCode);
        payment.setProviderContent(providerContent);
        payment.setPaidAt(paidAt == null ? LocalDateTime.now() : paidAt);
        if (payment.getFromAmount() == null) {
            payment.setFromAmount(BigDecimal.ZERO);
        }
        if (payment.getReceiveAmount() == null) {
            payment.setReceiveAmount(payment.getAmount());
        }

        return paymentTransactionRepo.save(payment);
    }

    private void validateRequest(PaymentTransferRequest request) {
        if (request == null) {
            throw new GlobalException(400, "Payment request is required");
        }
        if (request.getTransactionType() == null) {
            throw new GlobalException(400, "Transaction type is required");
        }
        if (request.getPaymentReferenceType() == null || request.getReferenceId() == null) {
            throw new GlobalException(400, "Payment reference is required");
        }
    }

    private BigDecimal normalize(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private UserWallet lockWallet(Long walletId, String message) {
        if (walletId == null) {
            throw new GlobalException(400, message);
        }
        return userWalletRepo.findByUserWalletIdForUpdate(walletId)
                .orElseThrow(() -> new GlobalException(404, message));
    }

    private void checkWalletActive(UserWallet wallet) {
        if (!UserWalletStatus.ACTIVE.equals(wallet.getStatus())) {
            throw new GlobalException(400, "Wallet is not active");
        }
        if (!"VND".equalsIgnoreCase(wallet.getCurrency())) {
            throw new GlobalException(400, "Wallet currency is not supported");
        }
    }

    private String resolvePaymentCode(PaymentTransferRequest request) {
        if (request.getPaymentCode() != null && !request.getPaymentCode().isBlank()) {
            return request.getPaymentCode();
        }

        return paymentCodeGenerator.generate("AIT-" + request.getTransactionType().name(), request.getReferenceId());
    }
}
