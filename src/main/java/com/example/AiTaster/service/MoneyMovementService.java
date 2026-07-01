package com.example.AiTaster.service;

import com.example.AiTaster.constant.*;
import com.example.AiTaster.entity.PaymentTransaction;
import com.example.AiTaster.entity.User;
import com.example.AiTaster.entity.UserWallet;
import com.example.AiTaster.exception.GlobalException;
import com.example.AiTaster.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MoneyMovementService {

    private final UserWalletRepo userWalletRepo;
    private final PaymentTransactionRepo paymentTransactionRepo;
    private final UserRepo userRepo;
    private final UserWalletService userWalletService;
    private final PlatformFeeCalculator platformFeeCalculator;
    private final ProjectEscrowBalanceService projectEscrowBalanceService;

    @Value("${app.platform.admin-username:admin}")
    private String adminUsername;
   //hàm này sử dụng trong nội bộ không cần tạo transaction pending như sepay.
    @Transactional
    public PaymentTransaction moneyTransactionManagement(

            Long fromId,   // bên gữi tiền
            Long toId,  // bên nhận tiền
            TransactionType transactionType, //nghiệp vụ nào
            Long referenceId, // đối tượng thanh toán
            PaymentReferenceType referenceType,
            String description,
            BigDecimal deductibleAmount, // số tiền bị trừ từ vi người gữi
            BigDecimal receivedAmount,   // số tiền được cộng từ ví người nhận
            Long transactionId
    ) {
        validatePaymentAmount(deductibleAmount, receivedAmount);

        //có người / ví nguồn và số tiền cần trừ lớn hơn không : cã hai ĐK đều đúng thì rút / trừ tiền từ ví user có Id là fromtId
        // và Lưu lại ID của ví nguồn vừa bị trừ tiền.
        //fromId có người gữi thì trừ tiền và đều sử lý nếu tiền lớn hơn 0
        Long sourceWalletId = withdrawMoney(fromId, deductibleAmount, transactionType);


        //có người nhận / hoặc ví đích và số tiền nhận được lớn hơn không : cả hai đều đúng thì cộng tiền vào ví của user đó
        //có người nhận thì cộng tiền và đều sử lý nếu tiền lớn hơn không
      Long targetWalletId = depositMoney(toId, receivedAmount, transactionType);

        //nếu cả 2 đều có id thì 1 bên đc trừ tiền và 1 bên được cộng tiền

    //nếu transaction khác null vậy là trước đó đã có payment transaction rồi cần update lên
        if (transactionId != null) {
            return updatePendingTransactionToSuccess(
                    transactionId,
                    fromId,
                    toId,
                    sourceWalletId,
                    targetWalletId,
                    transactionType,
                    referenceId,
                    referenceType,
                    description,
                    deductibleAmount,
                    receivedAmount
            );
        }

        return createSuccessTransaction(
                fromId,
                toId,
                sourceWalletId,
                targetWalletId,
                transactionType,
                referenceId,
                referenceType,
                description,
                deductibleAmount,
                receivedAmount
        );
    }

    @Transactional
    public BigDecimal calculateFee(BigDecimal amount) {
        validateAmount(amount, "Amount");

        BigDecimal feeAmount = platformFeeCalculator.calculatePlatformFee(amount);

        if (feeAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return amount;
        }

        User adminUser = userRepo.findByUsername(adminUsername)
                .orElseThrow(() -> new GlobalException(404, "Admin user not found"));

        UserWallet adminWallet = userWalletRepo.findByUserForUpdate(adminUser)
                .orElseThrow(() -> new GlobalException(404, "Admin wallet not found"));

        moneyTransactionManagement(
                null,
                adminUser.getUserId(),
                TransactionType.PLATFORM_FEE,
                adminWallet.getUserWalletId(),
                PaymentReferenceType.USER_WALLET,
                "Platform fee",
                BigDecimal.ZERO,
                feeAmount,
                null
        );

        return amount.subtract(feeAmount);
    }
    private PaymentTransaction updatePendingTransactionToSuccess(
            Long transactionId,
            Long fromId,
            Long toId,
            Long sourceWalletId,
            Long targetWalletId,
            TransactionType transactionType,
            Long referenceId,
            PaymentReferenceType referenceType,
            String description,
            BigDecimal deductibleAmount,
            BigDecimal receivedAmount
    ) {
        PaymentTransaction transaction = paymentTransactionRepo.findById(transactionId)
                .orElseThrow(() -> new GlobalException(404, "Payment transaction not found"));

        if (!PaymentStatus.PENDING.equals(transaction.getPaymentStatus())) {
            throw new GlobalException(400, "Payment transaction is not pending");
        }

        transaction.setSenderId(fromId);
        transaction.setReceiverId(toId);
        transaction.setSourceWalletId(sourceWalletId);
        transaction.setTargetWalletId(targetWalletId);
        transaction.setProjectEscrowId(resolveProjectEscrowId(transactionType, fromId, toId));
        transaction.setTransactionType(transactionType);
        transaction.setReferenceId(referenceId);
        transaction.setPaymentReferenceType(referenceType);
        transaction.setDescription(description);
        transaction.setPaymentStatus(PaymentStatus.SUCCESS);

        // Pending transaction da co grossAmount = amount tu luc tao.
        transaction.setFeeAmount(BigDecimal.ZERO);
        transaction.setNetAmount(receivedAmount);

        if (transaction.getGrossAmount() == null) {
            transaction.setGrossAmount(resolveGrossAmount(deductibleAmount, receivedAmount));
        }

        transaction.setPaidAt(LocalDateTime.now());

        return paymentTransactionRepo.save(transaction);
    }

    private PaymentTransaction createSuccessTransaction(
            Long fromId,
            Long toId,
            Long sourceWalletId,
            Long targetWalletId,
            TransactionType transactionType,
            Long referenceId,
            PaymentReferenceType referenceType,
            String description,
            BigDecimal deductibleAmount,
            BigDecimal receivedAmount
    ) {
        PaymentTransaction transaction = PaymentTransaction.builder()
                .senderId(fromId)
                .receiverId(toId)
                .sourceWalletId(sourceWalletId)
                .targetWalletId(targetWalletId)
                .projectEscrowId(resolveProjectEscrowId(transactionType, fromId, toId))
                .grossAmount(resolveGrossAmount(deductibleAmount, receivedAmount))
                .feeAmount(BigDecimal.ZERO)
                .netAmount(receivedAmount)
                .currency("VND")
                .transactionType(transactionType)
                .paymentMethod(PaymentMethod.WALLET)
                .paymentStatus(PaymentStatus.SUCCESS)
                .referenceId(referenceId)
                .paymentReferenceType(referenceType)
                .providerName("INTERNAL")
                .paymentCode(generateInternalPaymentCode(transactionType, referenceId))
                .description(description)
                .paidAt(LocalDateTime.now())
                .build();

        return paymentTransactionRepo.save(transaction);
    }
    private BigDecimal resolveGrossAmount(BigDecimal deductibleAmount, BigDecimal receivedAmount) {
        if (deductibleAmount.compareTo(BigDecimal.ZERO) > 0) {
            return deductibleAmount;
        }

        return receivedAmount;
    }



    private void validatePaymentAmount(BigDecimal deductibleAmount, BigDecimal receivedAmount) {
        if (deductibleAmount == null || receivedAmount == null) {
            throw new GlobalException(400, "Payment amount must not be null");
        }

        if (deductibleAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new GlobalException(400, "Deductible amount must not be negative");
        }

        if (receivedAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new GlobalException(400, "Received amount must not be negative");
        }
    }


    private String generateInternalPaymentCode(TransactionType transactionType,Long referenceId) {
     String prefix = switch (transactionType) {
         case PROJECT_ESCROW_DEPOSIT -> "AIT-ESC";
         case PROJECT_ESCROW_RELEASE -> "AIT-REL";
         case PROJECT_ESCROW_REFUND -> "AIT-RFD";
         case PROPOSAL_PURCHASE -> "AIT-PRO";
         case EXPERT_SERVICE_PURCHASE -> "AIT-SVC";
         case USER_WITHDRAW -> "AIT-WDR";
         case USER_DEPOSIT -> "AIT-DEP";
         case PLATFORM_FEE -> "AIT-FEE";
         default -> "AIT-INT";
     };
     String paymentCode = UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();
     return prefix + "-" + referenceId +  "-" + paymentCode;
    }
    private void validateAmount(BigDecimal amount, String fieldName) {
        if (amount == null) {
            throw new GlobalException(400, fieldName + " must not be null");
        }

        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new GlobalException(400, fieldName + " must not be negative");
        }
    }
    private Long withdrawMoney(Long fromId, BigDecimal amount, TransactionType transactionType) {
        if (fromId == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }

        if (TransactionType.PROJECT_ESCROW_RELEASE.equals(transactionType)
                || TransactionType.PROJECT_ESCROW_REFUND.equals(transactionType)) {
            projectEscrowBalanceService.withdrawByEscrowId(fromId, amount);
            return null;
        }

        return userWalletService
                .withdrawByUserId(fromId, amount)
                .getUserWalletId();
    }

    private Long depositMoney(Long toId, BigDecimal amount, TransactionType transactionType) {
        if (toId == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }

        if (TransactionType.PROJECT_ESCROW_DEPOSIT.equals(transactionType)) {
            projectEscrowBalanceService.depositByEscrowId(toId, amount);
            return null;
        }


        return userWalletService
                .depositByUserId(toId, amount)
                .getUserWalletId();
    }
    // Hàm này giúp transaction biết đang liên quan tới escrow nào.
    private Long resolveProjectEscrowId(TransactionType transactionType, Long fromId, Long toId) {
        return switch (transactionType) {
            case PROJECT_ESCROW_DEPOSIT -> toId;
            case PROJECT_ESCROW_RELEASE, PROJECT_ESCROW_REFUND -> fromId;
            default -> null;
        };
    }
}
