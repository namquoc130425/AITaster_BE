package com.example.AiTaster.service;
import com.example.AiTaster.constant.*;
import com.example.AiTaster.entity.*;
import com.example.AiTaster.exception.GlobalException;
import com.example.AiTaster.repository.PaymentTransactionRepo;
import com.example.AiTaster.repository.ProjectEscrowRepo;
import com.example.AiTaster.repository.UserRepo;
import com.example.AiTaster.repository.UserWalletRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class ProjectEscrowReleaseService {
    private final ProjectEscrowRepo projectEscrowRepo;
    private final UserWalletRepo userWalletRepo;
    private final UserRepo userRepo;
    private final PaymentTransactionRepo paymentTransactionRepo;
    private final PlatformFeeCalculator platformFeeCalculator;

    @Value("${app.platform.admin-username:admin}")
    private String adminUsername;

    @Transactional
    public ProjectEscrow releaseToExpert(Project project) {
        ProjectEscrow escrow = projectEscrowRepo.findByProjectIdForUpdate(project.getProjectId()).orElseThrow(() -> new GlobalException(404, "Project escrow not found"));

        if (!EscrowStatus.HELD.equals(escrow.getEscrowStatus())) {
            throw new GlobalException(400, "Escrow is not HELD");
        }

        BigDecimal heldAmount = escrow.getHeldAmount();

        if (heldAmount == null || heldAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new GlobalException(400, "Escrow held amount is invalid");
        }

        User expertUser = project.getInvitation().getExpertApplication().getExpertProfile().getUser();

        User adminUser = userRepo.findByUsername(adminUsername).orElseThrow(() -> new GlobalException(404, "Admin user not found"));

        UserWallet expertWallet = userWalletRepo.findByUserForUpdate(expertUser).orElseThrow(() -> new GlobalException(404, "Expert wallet not found"));

        UserWallet adminWallet = userWalletRepo.findByUserForUpdate(adminUser).orElseThrow(() -> new GlobalException(404, "Admin wallet not found"));

        checkWalletActive(expertWallet, "Expert wallet is not active");
        checkWalletActive(adminWallet, "Admin wallet is not active");

        BigDecimal platformFee = platformFeeCalculator.calculatePlatformFee(heldAmount);
        BigDecimal expertAmount = heldAmount.subtract(platformFee);

        if (expertAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new GlobalException(400, "Expert amount is invalid");
        }

        expertWallet.setBalance(expertWallet.getBalance().add(expertAmount));
        adminWallet.setBalance(adminWallet.getBalance().add(platformFee));

        escrow.setPlatformFee(platformFee);
        escrow.setExpertAmount(expertAmount);
        escrow.setEscrowStatus(EscrowStatus.RELEASED);

        userWalletRepo.save(expertWallet);
        userWalletRepo.save(adminWallet);

        paymentTransactionRepo.save(buildExpertReleaseTransaction(project, escrow, expertUser, expertWallet, expertAmount
                )
        );

        if (platformFee.compareTo(BigDecimal.ZERO) > 0) {
            paymentTransactionRepo.save(buildPlatformFeeTransaction(project, escrow, adminUser, adminWallet, platformFee));
        }

        return projectEscrowRepo.save(escrow);
    }

    private void checkWalletActive(UserWallet wallet, String message) {
        if (!UserWalletStatus.ACTIVE.equals(wallet.getStatus())) {
            throw new GlobalException(400, message);
        }

        if (!"VND".equalsIgnoreCase(wallet.getCurrency())) {
            throw new GlobalException(400, "Wallet currency is not supported");
        }
    }

    private PaymentTransaction buildExpertReleaseTransaction(Project project, ProjectEscrow escrow, User expertUser, UserWallet expertWallet, BigDecimal expertAmount
    ) {
        return PaymentTransaction.builder()
                .projectEscrowId(escrow.getProjectEscrowId())
                .expertServiceId(null)
                .senderId(null)
                .receiverId(expertUser.getUserId())
                .sourceWalletId(null)
                .targetWalletId(expertWallet.getUserWalletId())
                .amount(expertAmount)
                .currency("VND")
                .transactionType(TransactionType.PROJECT_ESCROW_RELEASE)
                .paymentMethod(PaymentMethod.WALLET)
                .paymentStatus(PaymentStatus.SUCCESS)
                .referenceId(project.getProjectId())
                .paymentReferenceType(PaymentReferenceType.PROJECT)
                .providerName("INTERNAL")
                .paymentCode(generateInternalPaymentCode("AIT-REL", project.getProjectId()))
                .providerTransactionCode(null)
                .providerContent("Release escrow to expert for project " + project.getProjectId())
                .paidAt(LocalDateTime.now())
                .expiredAt(null)
                .build();
    }

    private PaymentTransaction buildPlatformFeeTransaction(Project project, ProjectEscrow escrow, User adminUser, UserWallet adminWallet,
            BigDecimal platformFee
    ) {
        return PaymentTransaction.builder()
                .projectEscrowId(escrow.getProjectEscrowId())
                .expertServiceId(null)
                .senderId(null)
                .receiverId(adminUser.getUserId())
                .sourceWalletId(null)
                .targetWalletId(adminWallet.getUserWalletId())
                .amount(platformFee)
                .currency("VND")
                .transactionType(TransactionType.PLATFORM_FEE)
                .paymentMethod(PaymentMethod.WALLET)
                .paymentStatus(PaymentStatus.SUCCESS)
                .referenceId(project.getProjectId())
                .paymentReferenceType(PaymentReferenceType.PROJECT)
                .providerName("INTERNAL")
                .paymentCode(generateInternalPaymentCode("AIT-FEE", project.getProjectId()))
                .providerTransactionCode(null)
                .providerContent("Platform fee for project " + project.getProjectId())
                .paidAt(LocalDateTime.now())
                .expiredAt(null)
                .build();
    }

    private String generateInternalPaymentCode(String prefix, Long projectId) {
        String randomPart = UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 8)
                .toUpperCase();

        return prefix + "-" + projectId + "-" + randomPart;
    }
}
