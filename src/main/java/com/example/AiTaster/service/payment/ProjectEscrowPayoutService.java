package com.example.AiTaster.service.payment;
import com.example.AiTaster.constant.*;
import com.example.AiTaster.entity.*;
import com.example.AiTaster.exception.GlobalException;
import com.example.AiTaster.repository.ProjectEscrowRepo;
import com.example.AiTaster.repository.ProjectRepo;
import com.example.AiTaster.service.MoneyMovementService;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Service
@RequiredArgsConstructor
public class ProjectEscrowPayoutService {
    private final ProjectEscrowRepo projectEscrowRepo;
    private final MoneyMovementService moneyMovementService;
    private final ProjectRepo projectRepo;



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

        // calculateFee() se tu tao transaction PLATFORM_FEE cho admin,
        BigDecimal expertAmount = moneyMovementService.calculateFee(heldAmount);
        BigDecimal platformFee = heldAmount.subtract(expertAmount);

        if (expertAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new GlobalException(400, "Expert amount is invalid");
        }

       moneyMovementService.moneyTransactionManagement(
               escrow.getProjectEscrowId(),
               expertUser.getUserId(),
               TransactionType.PROJECT_ESCROW_RELEASE,
               project.getProjectId(),
               PaymentReferenceType.PROJECT,
               "Release escrow to expert - project " + project.getProjectId(),
                          heldAmount,
                         expertAmount,
                         null
       );

        escrow.setPlatformFee(platformFee);
        escrow.setExpertAmount(expertAmount);
        escrow.setEscrowStatus(EscrowStatus.RELEASED);

        project.setProjectStatus(ProjectStatus.COMPLETED);
        project.setCompletedAt(LocalDateTime.now());
        project.setIsActive(false);

        projectRepo.save(project);

        return projectEscrowRepo.save(escrow);
    }
    @Transactional
    public ProjectEscrow refundToClient(Project project) {
        ProjectEscrow escrow = projectEscrowRepo.findByProjectIdForUpdate(project.getProjectId()).orElseThrow(() -> new GlobalException(404, "Project escrow not found"));

        if (!EscrowStatus.HELD.equals(escrow.getEscrowStatus())) {
            throw new GlobalException(400, "Escrow is not HELD");
        }

        BigDecimal heldAmount = escrow.getHeldAmount();

        if (heldAmount == null || heldAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new GlobalException(400, "Escrow held amount is invalid");
        }

        User clientUser = project.getInvitation().getExpertApplication().getJobpost().getClientProfile().getUser();

        // Refund khong tinh phi:
        // escrow tra lai full tien cho client.
        moneyMovementService.moneyTransactionManagement(
                escrow.getProjectEscrowId(),
                clientUser.getUserId(),
                TransactionType.PROJECT_ESCROW_REFUND,
                project.getProjectId(),
                PaymentReferenceType.PROJECT,
                "Refund escrow to client - project " + project.getProjectId(),
                heldAmount,
                heldAmount,
                null
        );

        escrow.setEscrowStatus(EscrowStatus.REFUNDED);

        project.setProjectStatus(ProjectStatus.CANCELED);
        project.setIsActive(false);

        projectRepo.save(project);

        return projectEscrowRepo.save(escrow);
    }


}
