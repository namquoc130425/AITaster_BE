package com.example.AiTaster.service;

import com.example.AiTaster.constant.PaymentMethod;
import com.example.AiTaster.constant.PaymentReferenceType;
import com.example.AiTaster.constant.PaymentStatus;
import com.example.AiTaster.constant.TransactionType;
import com.example.AiTaster.constant.UserWalletStatus;
import com.example.AiTaster.dto.request.WalletBalanceRequest;
import com.example.AiTaster.dto.response.ProjectPaymentResponse;
import com.example.AiTaster.dto.response.SepayCheckoutFormResponse;
import com.example.AiTaster.dto.response.WalletDepositPaymentResponse;
import com.example.AiTaster.entity.PaymentTransaction;
import com.example.AiTaster.entity.User;
import com.example.AiTaster.entity.UserWallet;
import com.example.AiTaster.exception.GlobalException;
import com.example.AiTaster.mapper.PaymentTransactionMapper;
import com.example.AiTaster.repository.PaymentTransactionRepo;
import com.example.AiTaster.repository.UserWalletRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WalletDepositService {

    private final CurrentUserService currentUserService;
    private final UserWalletRepo userWalletRepo;
    private final PaymentTransactionRepo paymentTransactionRepo;
    private final PaymentTransactionMapper paymentTransactionMapper;
    private final SepayGateway sepayGateway;

    @Transactional
    public WalletDepositPaymentResponse createWalletDeposit(Long userWalletId, WalletBalanceRequest request) {
        User currentUser = currentUserService.getCurrentUser();
        UserWallet userWallet = findUserWalletById(userWalletId);

        checkWalletDeposit(userWallet, currentUser, request);

        PaymentTransaction paymentTransaction = paymentTransactionRepo.findPendingTransactionByReferenceAndMethod(
                PaymentReferenceType.USER_WALLET,
                userWallet.getUserWalletId(),
                PaymentStatus.PENDING,
                PaymentMethod.SEPAY
        ).map(existingPayment -> {
            if (existingPayment.getAmount().compareTo(request.getAmount()) == 0) {
                return existingPayment;
            }

            existingPayment.setPaymentStatus(PaymentStatus.EXPIRED);
            paymentTransactionRepo.save(existingPayment);
            return createPendingUserWalletId(userWallet, currentUser, request);
        }).orElseGet(() -> createPendingUserWalletId(userWallet, currentUser, request));

        SepayCheckoutFormResponse checkoutForm = sepayGateway.createCheckoutForm(paymentTransaction);
        return paymentTransactionMapper.toWalletDepositPaymentResponse(paymentTransaction, checkoutForm);
    }

    private void checkWalletDeposit(UserWallet userWallet, User currentUser, WalletBalanceRequest request) {
        if (request == null || request.getAmount() == null) {
            throw new GlobalException(400, "Amount is required");
        }

        if (!userWallet.getUser().getUserId().equals(currentUser.getUserId())) {
            throw new GlobalException(403, "You are not owner of this wallet");
        }

        if (userWallet.getStatus() != UserWalletStatus.ACTIVE) {
            throw new GlobalException(400, "Wallet is not active");
        }

        if (!"VND".equalsIgnoreCase(userWallet.getCurrency())) {
            throw new GlobalException(400, "Wallet currency is not supported");
        }

        if (request.getAmount().compareTo(new BigDecimal("10000")) < 0) {
            throw new GlobalException(400, "Minimum deposit must be 10.000d");
        }

        if (request.getAmount().compareTo(new BigDecimal("50000000")) > 0) {
            throw new GlobalException(400, "Maximum deposit is 50.000.000d");
        }

        if (request.getAmount().stripTrailingZeros().scale() > 0) {
            throw new GlobalException(400, "Amount must be integer");
        }
    }

    private PaymentTransaction createPendingUserWalletId(UserWallet userWallet, User userCurrent, WalletBalanceRequest request) {
        PaymentTransaction paymentTransaction = PaymentTransaction.builder()
                .projectEscrowId(null)
                .expertServiceId(null)
                .senderId(userCurrent.getUserId())
                .receiverId(userCurrent.getUserId())
                .sourceWalletId(null)
                .targetWalletId(userWallet.getUserWalletId())
                .amount(request.getAmount())
                .currency("VND")
                .transactionType(TransactionType.USER_DEPOSIT)
                .paymentMethod(PaymentMethod.SEPAY)
                .paymentStatus(PaymentStatus.PENDING)
                .referenceId(userWallet.getUserWalletId())
                .paymentReferenceType(PaymentReferenceType.USER_WALLET)
                .providerName("SEPAY")
                .paymentCode(generatePaymentCode(userWallet.getUserWalletId()))
                .providerTransactionCode(null)
                .providerContent(null)
                .paidAt(null)
                .expiredAt(LocalDateTime.now().plusHours(1))
                .build();
        return paymentTransactionRepo.save(paymentTransaction);
    }

    private String generatePaymentCode(Long userWalletId) {
        String randomPart = UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 8)
                .toUpperCase();
        return "AIT-WALLET-IN-" + userWalletId + "-" + randomPart;
    }

    private UserWallet findUserWalletById(Long userWalletId) {
        return userWalletRepo.findByUserWalletId(userWalletId)
                .orElseThrow(() -> new GlobalException(404, "User wallet not found"));
    }
}
