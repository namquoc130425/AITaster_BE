package com.example.AiTaster.service;
import com.example.AiTaster.constant.*;
import com.example.AiTaster.dto.request.PaymentTransferRequest;
import com.example.AiTaster.entity.*;
import com.example.AiTaster.exception.GlobalException;
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
    private final PlatformFeeCalculator platformFeeCalculator;
    private final PaymentTransferService paymentTransferService;

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

        escrow.setPlatformFee(platformFee);
        escrow.setExpertAmount(expertAmount);

        paymentTransferService.transfer(PaymentTransferRequest.builder()
                .projectEscrowId(escrow.getProjectEscrowId())
                .senderId(null)
                .receiverId(expertUser.getUserId())
                .sourceWalletId(null)
                .targetWalletId(expertWallet.getUserWalletId())
                .fromAmount(BigDecimal.ZERO)
                .receiveAmount(expertAmount)
                .transactionType(TransactionType.PROJECT_ESCROW_RELEASE)
                .paymentMethod(PaymentMethod.WALLET)
                .paymentStatus(PaymentStatus.SUCCESS)
                .referenceId(project.getProjectId())
                .paymentReferenceType(PaymentReferenceType.PROJECT)
                .providerName("INTERNAL")
                .paymentCode(generateInternalPaymentCode("AIT-REL", project.getProjectId()))
                .description("Release escrow to expert for project " + project.getProjectId())
                .creditTargetWallet(true)
                .build());

        if (platformFee.compareTo(BigDecimal.ZERO) > 0) {
            paymentTransferService.transfer(PaymentTransferRequest.builder()
                    .projectEscrowId(escrow.getProjectEscrowId())
                    .senderId(null)
                    .receiverId(adminUser.getUserId())
                    .sourceWalletId(null)
                    .targetWalletId(adminWallet.getUserWalletId())
                    .fromAmount(BigDecimal.ZERO)
                    .receiveAmount(platformFee)
                    .transactionType(TransactionType.PLATFORM_FEE)
                    .paymentMethod(PaymentMethod.WALLET)
                    .paymentStatus(PaymentStatus.SUCCESS)
                    .referenceId(project.getProjectId())
                    .paymentReferenceType(PaymentReferenceType.PROJECT)
                    .providerName("INTERNAL")
                    .paymentCode(generateInternalPaymentCode("AIT-FEE", project.getProjectId()))
                    .description("Platform fee for project " + project.getProjectId())
                    .creditTargetWallet(true)
                    .build());
        }

        escrow.setEscrowStatus(EscrowStatus.RELEASED);

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

    private String generateInternalPaymentCode(String prefix, Long projectId) {
        String randomPart = UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 8)
                .toUpperCase();

        return prefix + "-" + projectId + "-" + randomPart;
    }
}
