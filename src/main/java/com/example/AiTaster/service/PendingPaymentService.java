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
    public PaymentTransaction createPendingPaymentTransaction(
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
        validatePendingAmount(amount);

<<<<<<< HEAD
        return paymentTransactionRepo.findPendingTransactionByReferenceAndMethod(
=======
        return paymentTransactionRepo.findPendingTransactionByReferenceAndMethod( // tìm payment Pendding cũ : có 1 số thông tin v..v
>>>>>>> 4ceb432e65237a7ca034898d24e678aac4935384
                referenceType,
                referenceId,
                transactionType,
                senderId,
                PaymentStatus.PENDING,
                PaymentMethod.SEPAY
<<<<<<< HEAD
        ).map(existingPayment -> {
=======
        ).map(existingPayment -> {     // nếu tìm thấy kiểm tra payment pendding đó còn hạn hay không / không set status lưu lại
>>>>>>> 4ceb432e65237a7ca034898d24e678aac4935384
            if (existingPayment.getExpiredAt() != null
                    && existingPayment.getExpiredAt().isBefore(LocalDateTime.now())) {
                existingPayment.setPaymentStatus(PaymentStatus.EXPIRED);
                paymentTransactionRepo.save(existingPayment);

<<<<<<< HEAD
                return createNewPendingPayment(
=======
                return createNewPendingPayment( //Link thanh toán cũ hết hạn rồi, không dùng lại nữa. sau đó tạo payment transaction mới
>>>>>>> 4ceb432e65237a7ca034898d24e678aac4935384
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

<<<<<<< HEAD
            if (existingPayment.getGrossAmount().compareTo(amount) == 0) {
                return existingPayment;
            }
=======
            if (existingPayment.getGrossAmount().compareTo(amount) == 0) { // th2 : payment cũng còn hạn và cùng giá tiền -> trả payment cũ
                return existingPayment;
            }                                                              //th3 : payment còn hạn nhưng khác giá tiền -> set status cũ là EXPIRED và tạo   payment mới
>>>>>>> 4ceb432e65237a7ca034898d24e678aac4935384

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
<<<<<<< HEAD
        }).orElseGet(() -> createNewPendingPayment(
=======
        }).orElseGet(() -> createNewPendingPayment( // Không có payment PENDING nào phù hợp => tạo payment mới
>>>>>>> 4ceb432e65237a7ca034898d24e678aac4935384
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
