package com.example.AiTaster.service.payment;


import com.example.AiTaster.constant.*;
import com.example.AiTaster.dto.response.ProjectPaymentResponse;
import com.example.AiTaster.dto.response.SepayCheckoutFormResponse;
import com.example.AiTaster.entity.*;
import com.example.AiTaster.exception.GlobalException;
import com.example.AiTaster.mapper.PaymentTransactionMapper;
import com.example.AiTaster.repository.*;
import com.example.AiTaster.service.*;
import com.example.AiTaster.service.imp.IProjectPayment;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Service
@RequiredArgsConstructor
public class ProjectPaymentService implements IProjectPayment {

    private final PaymentTransactionRepo paymentTransactionRepo ;
    private final PaymentTransactionMapper paymentTransactionMapper;
    private final CurrentUserService currentUserService;
    private final ClientProfileRepo clientProfileRepo;
    private final InvitationRepo invitationRepo;
    private final SepayGateway sepayGateway;
    private final PendingPaymentService pendingPaymentService;
    private final ProjectRepo projectRepo;
    private final ProjectEscrowRepo projectEscrowRepo;
    private final ProjectMilestoneService projectMilestoneService;
    private final PlatformFeeCalculator platformFeeCalculator;
    private final MoneyMovementService moneyMovementService;
    private final ConversationService conversationService;
    private final RealtimeService realtimeService;

    @Transactional
    @Override
    public ProjectPaymentResponse createProjectPayment(Long invitationId) {


        User currentUser = currentUserService.getCurrentUser();
        ClientProfile clientProfile = findClientProfileByCurrentUser(currentUser);
        Invitation invitation = findInvitation(invitationId);
        checkInvitationOwnerClient(invitation, clientProfile);
        if (projectRepo.existsByInvitation(invitation)) {
            throw new GlobalException(400, "Project already exists for this invitation");
        }
        ensureInvitationCanBePaid(invitation);


        // Nếu đã có pending SePay còn hạn thì dùng lại, nếu không thì tạo mới.
        PaymentTransaction paymentTransaction = pendingPaymentService.createPendingPaymentTransaction(
                currentUser.getUserId(),
                null,
                null,
                null,
                null,
                null,
                TransactionType.PROJECT_ESCROW_DEPOSIT,
                invitationId,
                PaymentReferenceType.INVITATION,
                invitation.getFinalOfferedPrice(),
                "SePay project escrow payment - invitation " + invitation.getInvitationId(),
                invitation.getRespondedAt().plusHours(24)

        );
        SepayCheckoutFormResponse checkoutForm = sepayGateway.createCheckoutForm(paymentTransaction);
        // Trả về đầy đủ dữ liệu để FE render hidden checkout form và submit.
        return paymentTransactionMapper.toInvitationPaymentResponse(
                paymentTransaction,
                invitation.getInvitationId(),
                checkoutForm);
    }



    // Kiểm tra invitation có thuộc client hiện tại không.
    private void checkInvitationOwnerClient(Invitation invitation, ClientProfile clientProfile) {
        Long ownerClientId = invitation.getExpertApplication()
                .getJobpost()
                .getClientProfile()
                .getClientProfileId();

        if (!ownerClientId.equals(clientProfile.getClientProfileId())) {
            throw new GlobalException(403, "You are not owner client of this invitation");
        }
    }


    // Tìm invitation theo id.
    private Invitation findInvitation(Long invitationId) {
        return invitationRepo.findByInvitationId(invitationId)
                .orElseThrow(() -> new GlobalException(404, "Invitation not found"));
    }


    // Invitation phải được expert accept, có thời gian phản hồi, và còn hạn thanh toán.
    private void ensureInvitationCanBePaid(Invitation invitation) {
        if (!InvitationStatus.ACCEPTED.equals(invitation.getInvitationStatus())) {
            throw new GlobalException(400, "Invitation is not accepted");
        }

        if (invitation.getRespondedAt() == null) {
            throw new GlobalException(400, "Invitation response time is missing");
        }

        if (invitation.getRespondedAt().plusHours(24).isBefore(LocalDateTime.now())) {
            expireInvitationPayment(invitation);
            throw new GlobalException(403, "Payment deadline is expired");
        }
    }

    private void expireInvitationPayment(Invitation invitation) {
        invitation.setInvitationStatus(InvitationStatus.PAYMENT_EXPIRED);
        invitationRepo.save(invitation);

        expirePendingInvitationSepayPayment(
                invitation,
                invitation.getExpertApplication()
                        .getJobpost()
                        .getClientProfile()
                        .getUser()
                        .getUserId()
        );
    }

    private void expirePendingInvitationSepayPayment(Invitation invitation, Long senderId) {
        paymentTransactionRepo.findPendingTransactionByReferenceAndMethod(
                PaymentReferenceType.INVITATION,
                invitation.getInvitationId(),
                TransactionType.PROJECT_ESCROW_DEPOSIT,
                senderId,
                PaymentStatus.PENDING,
                PaymentMethod.SEPAY
        ).ifPresent(payment -> {
            payment.setPaymentStatus(PaymentStatus.EXPIRED);
            paymentTransactionRepo.save(payment);
        });
    }

    private ClientProfile findClientProfileByCurrentUser(User currentUser) {
        return clientProfileRepo.findByUser(currentUser).orElseThrow(() -> new GlobalException(403, "Only client can create project payment"));
    }

    @Transactional
    public ProjectPaymentResponse createProjectPaymentByWallet(Long invitationId) {
        User currentUser = currentUserService.getCurrentUser();
        ClientProfile clientProfile = findClientProfileByCurrentUser(currentUser);

        Invitation invitation = invitationRepo.findWithDetailByInvitationId(invitationId)
                .orElseThrow(() -> new GlobalException(404, "Invitation not found"));

        checkInvitationOwnerClient(invitation, clientProfile);
        ensureInvitationCanBePaid(invitation);

        if (projectRepo.existsByInvitation(invitation)) {
            throw new GlobalException(400, "Project already exists for this invitation");
        }

        Project project = createProjectByInvitation(invitation);
        ProjectEscrow escrow = createProjectEscrow(project);

        projectMilestoneService.createMilestoneForProject(project);
        conversationService.attachProject(
                invitation.getExpertApplication().getApplicationId(),
                project.getProjectId()
        );

        BigDecimal amount = invitation.getFinalOfferedPrice();

        // Thanh toán bằng ví:
        // fromId = client userId -> trừ ví client
        // toId = escrowId -> cộng tiền vào project escrow
        // transactionId = null vì ví xử lý thành công ngay.
        PaymentTransaction paymentTransaction = moneyMovementService.moneyTransactionManagement(
                currentUser.getUserId(),
                escrow.getProjectEscrowId(),
                TransactionType.PROJECT_ESCROW_DEPOSIT,
                project.getProjectId(),
                PaymentReferenceType.PROJECT,
                "Wallet project escrow deposit - project " + project.getProjectId(),
                amount,
                amount,
                null
        );

        LocalDateTime paidAt = paymentTransaction.getPaidAt();

        project.setIsActive(true);
        project.setStartAt(paidAt);
        project.setDeadlineAt(setUpDeadline(
                paidAt,
                project.getTimelineValue(),
                project.getTimelineUnit()
        ));

        escrow.setStartedAt(paidAt);
        escrow.setEscrowStatus(EscrowStatus.HELD);

        projectRepo.save(project);
        projectEscrowRepo.save(escrow);
        expirePendingInvitationSepayPayment(invitation, currentUser.getUserId());
        realtimeService.pushUserWalletEvent(
                currentUser,
                "PROJECT_ESCROW_PAID_BY_WALLET",
                null,
                "Project escrow paid by wallet"
        );
        realtimeService.pushProjectParticipants(
                project,
                "PROJECT_CREATED",
                "Project created"
        );

        return paymentTransactionMapper.toInvitationPaymentResponse(
                paymentTransaction,
                invitation.getInvitationId(),
                null
        );
    }

    private Project createProjectByInvitation(Invitation invitation) {
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
                .paymentDeadlineAt(invitation.getRespondedAt().plusHours(24))
                .projectStatus(ProjectStatus.ACTIVE)
                .isActive(false)
                .build();

        return projectRepo.save(project);
    }

    // Tạo project escrow.
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
                .escrowStatus(EscrowStatus.WAITING_PAYMENT)
                .startedAt(null)
                .build();

        return projectEscrowRepo.save(escrow);
    }

    // Tính deadline project từ thời điểm thanh toán và timeline đã thống nhất.
    private LocalDateTime setUpDeadline(LocalDateTime paidAt, Integer value, TimelineUnit timelineUnit) {
        if (paidAt == null || value == null || timelineUnit == null) {
            return null;
        }

        return switch (timelineUnit) {
            case DAY -> paidAt.plusDays(value);
            case WEEK -> paidAt.plusWeeks(value);
            case MONTH -> paidAt.plusMonths(value);
        };
    }


}
