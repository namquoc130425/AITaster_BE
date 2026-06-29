package com.example.AiTaster.service.payment.sepay;

import com.example.AiTaster.constant.*;
import com.example.AiTaster.dto.request.SepayWebhookRequest;
import com.example.AiTaster.entity.Invitation;
import com.example.AiTaster.entity.PaymentTransaction;
import com.example.AiTaster.entity.Project;
import com.example.AiTaster.entity.ProjectEscrow;
import com.example.AiTaster.exception.GlobalException;
import com.example.AiTaster.repository.InvitationRepo;
import com.example.AiTaster.repository.PaymentTransactionRepo;
import com.example.AiTaster.repository.ProjectEscrowRepo;
import com.example.AiTaster.repository.ProjectRepo;
import com.example.AiTaster.service.PlatformFeeCalculator;
import com.example.AiTaster.service.ProjectMilestoneService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
@Component
@RequiredArgsConstructor
public class ProjectEscrowPaymentHandler implements SepayPaymentHandler {
    private final InvitationRepo invitationRepo;
    private final ProjectRepo projectRepo;
    private final ProjectEscrowRepo projectEscrowRepo;
    private final PaymentTransactionRepo paymentTransactionRepo;
    private final ProjectMilestoneService projectMilestoneService   ;
    private final PlatformFeeCalculator platformFeeCalculator;
    @Override
    public boolean supports(PaymentTransaction payment) {
        return PaymentMethod.SEPAY.equals(payment.getPaymentMethod())
                && PaymentReferenceType.INVITATION.equals(payment.getPaymentReferenceType())
                && TransactionType.PROJECT_ESCROW_DEPOSIT.equals(payment.getTransactionType());
    }

    @Override
    public void handle(PaymentTransaction payment, SepayWebhookRequest request, String providerTransactionCode, String providerContent, LocalDateTime paidAt
    ) {
        Invitation invitation = invitationRepo.findWithDetailByInvitationId(payment.getReferenceId())
                .orElse(null);

        if (invitation == null || !InvitationStatus.ACCEPTED.equals(invitation.getInvitationStatus())) {
            markFailed(payment, request, providerTransactionCode);
            return;
        }
        Project newProject = createProjectByExpertAcceptInvitation(invitation,payment);
        ProjectEscrow newProjectEscrow = createProjectEscrow(newProject);

        projectMilestoneService.createMilestoneForProject(newProject);

        payment.setPaymentStatus(PaymentStatus.SUCCESS);

        payment.setProviderTransactionCode(providerTransactionCode);

        payment.setProviderContent(providerContent);

        payment.setPaidAt(paidAt);

        payment.setProjectEscrowId(newProjectEscrow.getProjectEscrowId());
        payment.setFromAmount(BigDecimal.ZERO);
        payment.setReceiveAmount(payment.getAmount());
        payment.setDescription("Escrow deposit for project " + newProject.getProjectId());

        newProjectEscrow.setHeldAmount(payment.getAmount());

        newProjectEscrow.setStartedAt(paidAt);

        newProject.setIsActive(true);

        newProject.setStartAt(paidAt);

        newProject.setDeadlineAt(setUpDeadlike(paidAt,newProject.getTimelineValue(),newProject.getTimelineUnit()));

        paymentTransactionRepo.save(payment);

        projectEscrowRepo.save(newProjectEscrow);

        projectRepo.save(newProject);
    }
    private ProjectEscrow createProjectEscrow(Project project) {
        if (projectEscrowRepo.existsByProjectId(project.getProjectId())) {
            throw new GlobalException(400, "Project escrow already exists");
        }

        Long clientProfileId = project.getInvitation()
                .getExpertApplication()
                .getJobpost()
                .getClientProfile()
                .getClientProfileId();

        Long expertProfileId = project.getInvitation()
                .getExpertApplication()
                .getExpertProfile()
                .getExpertProfileId();
        BigDecimal agreedAmount = project.getAgreedPrice();
        BigDecimal platformFee = platformFeeCalculator.calculatePlatformFee(agreedAmount);
        BigDecimal expertAmount = agreedAmount.subtract(platformFee);

        ProjectEscrow escrow = ProjectEscrow.builder()
                .projectId(project.getProjectId())
                .clientProfileId(clientProfileId)
                .expertProfileId(expertProfileId)
                .agreedAmount(agreedAmount)
                .heldAmount(BigDecimal.ZERO)
                .platformFee(platformFee)
                .expertAmount(expertAmount)
                .escrowStatus(EscrowStatus.HELD)
                .startedAt(null)
                .build();

        return projectEscrowRepo.save(escrow);
    }

    private Project createProjectByExpertAcceptInvitation(Invitation invitation, PaymentTransaction payment) {
        if (projectRepo.existsByInvitation(invitation)) {
            throw new GlobalException(400, "Project already exists for this invitation");
        }
        Project project = Project.builder()
                .invitation(invitation)
                .title(invitation.getProjectTitle())
                .finalRequirementSnapshot(invitation.getFinalRequirement())
                .expectedOutputSnapshot(invitation.getExpectedOutput())
                .acceptanceCriteriaSnapshot(invitation.getAcceptanceCriteria())
                .agreedPrice(invitation.getFinalOfferedPrice())
                .timeline(invitation.getFinalTimeline())
                .timelineValue(invitation.getFinalTimelineValue())
                .timelineUnit(invitation.getFinalTimelineUnit())
                .deadlineAt(null)
                .startAt(null)
                .completedAt(null)
                .paymentDeadlineAt(payment.getExpiredAt())
                .projectStatus(ProjectStatus.ACTIVE)
                .isActive(false)
                .build();
        return projectRepo.save(project);
    }

    private LocalDateTime setUpDeadlike(LocalDateTime paidAt, Integer value, TimelineUnit timelineUnit) {
        TimelineUnit unit;
        if (paidAt == null || value == null || timelineUnit == null) {
            return null;
        }

        return switch (timelineUnit) {
            case DAY -> paidAt.plusDays(value);
            case WEEK -> paidAt.plusWeeks(value);
            case MONTH -> paidAt.plusMonths(value);
        };
    }



    // thanh toán thất bại set status payment , providerTransactionCode từ sepay gữi về ,ProviderContent từ lúc gữi đi cho sepay
    private void markFailed(PaymentTransaction paymentTransaction, SepayWebhookRequest sepayWebhookRequest, String providerTransactionCode) {
        paymentTransaction.setPaymentStatus(PaymentStatus.FAILED);
        paymentTransaction.setProviderTransactionCode(providerTransactionCode);
        paymentTransaction.setProviderContent(buildProviderContent(sepayWebhookRequest));;
        paymentTransactionRepo.save(paymentTransaction);
    }

    private String buildProviderContent(SepayWebhookRequest sepayWebhookRequest) {
        if (sepayWebhookRequest == null || sepayWebhookRequest.getOrder() == null) {
            return null;
        }

        return firstNotBlank(
                sepayWebhookRequest.getOrder().getOrderDescription(),
                sepayWebhookRequest.getOrder().getOrderInvoiceNumber(),
                sepayWebhookRequest.getOrder().getOrderId()
        );


    }
    // lấy chuổi đầu tiên không null và không rỗng
    private String firstNotBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

}
