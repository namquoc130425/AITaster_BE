package com.example.AiTaster.service.payment.sepay;
import com.example.AiTaster.constant.*;
import com.example.AiTaster.entity.*;
import com.example.AiTaster.exception.GlobalException;
import com.example.AiTaster.repository.ProjectEscrowRepo;
import com.example.AiTaster.service.MoneyMovementService;
import com.example.AiTaster.service.PlatformFeeCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;


@Service
@RequiredArgsConstructor
public class ProjectEscrowReleaseService {
    private final ProjectEscrowRepo projectEscrowRepo;
    private final PlatformFeeCalculator platformFeeCalculator;
    private final MoneyMovementService moneyMovementService;

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


        return projectEscrowRepo.save(escrow);
    }




}
