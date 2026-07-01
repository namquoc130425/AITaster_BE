package com.example.AiTaster.service;

import com.example.AiTaster.constant.PaymentMethod;
import com.example.AiTaster.constant.PaymentReferenceType;
import com.example.AiTaster.constant.PaymentStatus;
import com.example.AiTaster.constant.TransactionType;
import com.example.AiTaster.entity.PaymentTransaction;
import com.example.AiTaster.exception.GlobalException;
import com.example.AiTaster.repository.PaymentTransactionRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PendingPaymentService {
    private final PaymentTransactionRepo paymentTransactionRepo;

    @Transactional
    public PaymentTransaction createPendingPaymentTransaction(Long senderId,
                                                              Long receiverId,
                                                              Long sourceWalletId,
                                                              Long targetWalletId,
                                                              Long projectEscrowId,
                                                              Long expertServiceId,
                                                              TransactionType transactionType,
                                                              Long referenceId,
                                                              PaymentReferenceType referenceType,
                                                              BigDecimal amount,
                                                              String description,
                                                              LocalDateTime expiredAt) {
                validatePendingAmount(amount);
                //tìm paymentTransaction có pending cũ,method SEPAY v.v.v.

                return paymentTransactionRepo.findPendingTransactionByReferenceAndMethod(
                        referenceType,
                        referenceId,
                        transactionType,
                        senderId,
                        PaymentStatus.PENDING,
                        PaymentMethod.SEPAY
                ).map(existingPayment  -> { // nếu paymentTransaction có pending cũ == amount cu thì trả về Pendding cũ
                    if (existingPayment.getExpiredAt() != null
                            && existingPayment.getExpiredAt().isBefore(LocalDateTime.now())) { // nếu hết time thì set expired và tạo thanh toán, transaction mới
                        existingPayment.setPaymentStatus(PaymentStatus.EXPIRED);
                        paymentTransactionRepo.save(existingPayment);
                        return createNewPendingPayment(
                                senderId,
                                receiverId,
                                sourceWalletId,
                                targetWalletId,
                                projectEscrowId,
                                expertServiceId,
                                transactionType,
                                referenceId,
                                referenceType,
                                amount,
                                description,
                                expiredAt
                        );
                    }
                    if (existingPayment.getGrossAmount().compareTo(amount) == 0) {  // Pending cũ chưa hết hạn và cùng amount thì dùng lại.
                        return existingPayment;
                    }

                    existingPayment.setPaymentStatus(PaymentStatus.EXPIRED);
                    paymentTransactionRepo.save(existingPayment);


                    return createNewPendingPayment(
                            senderId,
                            receiverId,
                            sourceWalletId,
                            targetWalletId,
                            projectEscrowId,
                            expertServiceId,
                            transactionType,
                            referenceId,
                            referenceType,
                            amount,
                            description,
                            expiredAt

                    );
                }).orElseGet(() -> createNewPendingPayment( // tạo pendding mới và trả về pendding mới
                        senderId,
                        receiverId,
                        sourceWalletId,
                        targetWalletId,
                        projectEscrowId,
                        expertServiceId,
                        transactionType,
                        referenceId,
                        referenceType,
                        amount,
                        description,
                        expiredAt
                ));


    }

    private PaymentTransaction createNewPendingPayment(
            Long senderId,
            Long receiverId,
            Long sourceWalletId,
            Long targetWalletId,
            Long projectEscrowId,
            Long expertServiceId,
            TransactionType transactionType,
            Long referenceId,
            PaymentReferenceType referenceType,
            BigDecimal amount,
            String description,
            LocalDateTime expiredAt
    ) {
        PaymentTransaction paymentTransaction = PaymentTransaction.builder()
                .projectEscrowId(projectEscrowId)
                .expertServiceId(expertServiceId)
                .senderId(senderId)
                .receiverId(receiverId)
                .sourceWalletId(sourceWalletId)
                .targetWalletId(targetWalletId)
                .grossAmount(amount)
                .feeAmount(BigDecimal.ZERO)
                .netAmount(amount)
                .currency("VND")
                .transactionType(transactionType)
                .paymentMethod(PaymentMethod.SEPAY)
                .paymentStatus(PaymentStatus.PENDING)
                .referenceId(referenceId)
                .paymentReferenceType(referenceType)
                .providerName("SEPAY")
                .paymentCode(generatePaymentCode(transactionType, referenceType, referenceId))
                .providerTransactionCode(null)
                .providerContent(null)
                .description(description)
                .paidAt(null)
                .expiredAt(expiredAt)
                .build();

        return paymentTransactionRepo.save(paymentTransaction);
    }

    private String generatePaymentCode(
            TransactionType transactionType,
            PaymentReferenceType referenceType,
            Long referenceId
    ) {
        String randomPart = UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 8)
                .toUpperCase();

        if (TransactionType.USER_DEPOSIT.equals(transactionType)
                && PaymentReferenceType.USER_WALLET.equals(referenceType)) {
            return "AIT-WALLET-IN-" + referenceId + "-" + randomPart;
        }

        if (TransactionType.PROJECT_ESCROW_DEPOSIT.equals(transactionType)
                && PaymentReferenceType.INVITATION.equals(referenceType)) {
            return "AIT-INV-" + referenceId + "-" + randomPart;
        }

        return "AIT-PAY-" + referenceId + "-" + randomPart;
    }

    private void validatePendingAmount(BigDecimal amount) {
        if (amount == null) {
            throw new GlobalException(400, "Amount must not be null");
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new GlobalException(400, "Amount must be greater than zero");
        }
    }

}
