package com.example.AiTaster.service.payment;

import com.example.AiTaster.constant.InvitationStatus;
import com.example.AiTaster.constant.PaymentMethod;
import com.example.AiTaster.constant.PaymentReferenceType;
import com.example.AiTaster.constant.PaymentStatus;
import com.example.AiTaster.constant.TimelineUnit;
import com.example.AiTaster.constant.TransactionType;
import com.example.AiTaster.entity.ClientProfile;
import com.example.AiTaster.entity.ExpertApplication;
import com.example.AiTaster.entity.ExpertProfile;
import com.example.AiTaster.entity.Invitation;
import com.example.AiTaster.entity.JobPost;
import com.example.AiTaster.entity.PaymentTransaction;
import com.example.AiTaster.entity.Project;
import com.example.AiTaster.entity.ProjectEscrow;
import com.example.AiTaster.entity.User;
import com.example.AiTaster.exception.GlobalException;
import com.example.AiTaster.mapper.PaymentTransactionMapper;
import com.example.AiTaster.repository.ClientProfileRepo;
import com.example.AiTaster.repository.InvitationRepo;
import com.example.AiTaster.repository.PaymentTransactionRepo;
import com.example.AiTaster.repository.ProjectEscrowRepo;
import com.example.AiTaster.repository.ProjectRepo;
import com.example.AiTaster.service.ConversationService;
import com.example.AiTaster.service.CurrentUserService;
import com.example.AiTaster.service.MoneyMovementService;
import com.example.AiTaster.service.NotificationService;
import com.example.AiTaster.service.PendingPaymentService;
import com.example.AiTaster.service.PlatformFeeCalculator;
import com.example.AiTaster.service.ProjectMilestoneService;
import com.example.AiTaster.service.RealtimeService;
import com.example.AiTaster.service.SepayGateway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectPaymentServiceTest {

    @Mock
    private PaymentTransactionRepo paymentTransactionRepo;

    @Mock
    private PaymentTransactionMapper paymentTransactionMapper;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private ClientProfileRepo clientProfileRepo;

    @Mock
    private InvitationRepo invitationRepo;

    @Mock
    private SepayGateway sepayGateway;

    @Mock
    private PendingPaymentService pendingPaymentService;

    @Mock
    private ProjectRepo projectRepo;

    @Mock
    private ProjectEscrowRepo projectEscrowRepo;

    @Mock
    private ProjectMilestoneService projectMilestoneService;

    @Mock
    private PlatformFeeCalculator platformFeeCalculator;

    @Mock
    private MoneyMovementService moneyMovementService;

    @Mock
    private ConversationService conversationService;

    @Mock
    private RealtimeService realtimeService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private ProjectPaymentService projectPaymentService;

    @Test
    void createProjectPayment_blocksBeforeExpiringInvitationWhenProjectAlreadyExists() {
        User clientUser = User.builder()
                .userId(10L)
                .build();
        ClientProfile clientProfile = ClientProfile.builder()
                .clientProfileId(20L)
                .user(clientUser)
                .build();
        Invitation invitation = acceptedInvitation(clientProfile, LocalDateTime.now().minusHours(25));

        when(currentUserService.getCurrentUser()).thenReturn(clientUser);
        when(clientProfileRepo.findByUser(clientUser)).thenReturn(Optional.of(clientProfile));
        when(invitationRepo.findByInvitationId(1L)).thenReturn(Optional.of(invitation));
        when(projectRepo.existsByInvitation(invitation)).thenReturn(true);

        assertThatThrownBy(() -> projectPaymentService.createProjectPayment(1L))
                .isInstanceOf(GlobalException.class)
                .hasMessage("Project already exists for this invitation");

        assertThat(invitation.getInvitationStatus()).isEqualTo(InvitationStatus.ACCEPTED);
        verify(invitationRepo, never()).save(invitation);
        verify(paymentTransactionRepo, never()).findPendingTransactionByReferenceAndMethod(
                any(),
                any(),
                any(),
                any(),
                any(),
                any()
        );
    }

    @Test
    void createProjectPaymentByWallet_blocksBeforeExpiringInvitationWhenProjectAlreadyExists() {
        User clientUser = User.builder()
                .userId(10L)
                .build();
        ClientProfile clientProfile = ClientProfile.builder()
                .clientProfileId(20L)
                .user(clientUser)
                .build();
        Invitation invitation = acceptedInvitation(clientProfile, LocalDateTime.now().minusHours(25));

        when(currentUserService.getCurrentUser()).thenReturn(clientUser);
        when(clientProfileRepo.findByUser(clientUser)).thenReturn(Optional.of(clientProfile));
        when(invitationRepo.findWithDetailByInvitationId(1L)).thenReturn(Optional.of(invitation));
        when(projectRepo.existsByInvitation(invitation)).thenReturn(true);

        assertThatThrownBy(() -> projectPaymentService.createProjectPaymentByWallet(1L))
                .isInstanceOf(GlobalException.class)
                .hasMessage("Project already exists for this invitation");

        assertThat(invitation.getInvitationStatus()).isEqualTo(InvitationStatus.ACCEPTED);
        verify(invitationRepo, never()).save(invitation);
        verify(moneyMovementService, never()).moneyTransactionManagement(
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any()
        );
    }

    @Test
    void createProjectPayment_expiresInvitationAndPendingSepayPaymentWhenPaymentDeadlinePassed() {
        User clientUser = User.builder()
                .userId(10L)
                .build();
        ClientProfile clientProfile = ClientProfile.builder()
                .clientProfileId(20L)
                .user(clientUser)
                .build();
        Invitation invitation = acceptedInvitation(clientProfile, LocalDateTime.now().minusHours(25));
        PaymentTransaction pendingPayment = PaymentTransaction.builder()
                .paymentStatus(PaymentStatus.PENDING)
                .build();

        when(currentUserService.getCurrentUser()).thenReturn(clientUser);
        when(clientProfileRepo.findByUser(clientUser)).thenReturn(Optional.of(clientProfile));
        when(invitationRepo.findByInvitationId(1L)).thenReturn(Optional.of(invitation));
        when(projectRepo.existsByInvitation(invitation)).thenReturn(false);
        when(paymentTransactionRepo.findPendingTransactionByReferenceAndMethod(
                eq(PaymentReferenceType.INVITATION),
                eq(1L),
                eq(TransactionType.PROJECT_ESCROW_DEPOSIT),
                eq(10L),
                eq(PaymentStatus.PENDING),
                eq(PaymentMethod.SEPAY)
        )).thenReturn(Optional.of(pendingPayment));

        assertThatThrownBy(() -> projectPaymentService.createProjectPayment(1L))
                .isInstanceOf(GlobalException.class)
                .hasMessage("Payment deadline is expired");

        assertThat(invitation.getInvitationStatus()).isEqualTo(InvitationStatus.PAYMENT_EXPIRED);
        assertThat(pendingPayment.getPaymentStatus()).isEqualTo(PaymentStatus.EXPIRED);
        verify(invitationRepo).save(invitation);
        verify(paymentTransactionRepo).save(pendingPayment);
    }

    @Test
    void createProjectPaymentByWallet_expiresExistingPendingSepayPaymentWhenWalletPaymentSucceeds() {
        User clientUser = User.builder()
                .userId(10L)
                .build();
        ClientProfile clientProfile = ClientProfile.builder()
                .clientProfileId(20L)
                .user(clientUser)
                .build();
        Invitation invitation = acceptedInvitation(clientProfile, LocalDateTime.now().minusHours(1));
        PaymentTransaction pendingSepayPayment = PaymentTransaction.builder()
                .paymentStatus(PaymentStatus.PENDING)
                .build();
        PaymentTransaction walletSuccessPayment = PaymentTransaction.builder()
                .paidAt(LocalDateTime.now())
                .paymentStatus(PaymentStatus.SUCCESS)
                .build();

        when(currentUserService.getCurrentUser()).thenReturn(clientUser);
        when(clientProfileRepo.findByUser(clientUser)).thenReturn(Optional.of(clientProfile));
        when(invitationRepo.findWithDetailByInvitationId(1L)).thenReturn(Optional.of(invitation));
        when(projectRepo.existsByInvitation(invitation)).thenReturn(false);
        when(projectRepo.save(any(Project.class))).thenAnswer(invocation -> {
            Project project = invocation.getArgument(0);
            if (project.getProjectId() == null) {
                project.setProjectId(100L);
            }
            return project;
        });
        when(projectEscrowRepo.existsByProjectId(100L)).thenReturn(false);
        when(platformFeeCalculator.calculatePlatformFee(invitation.getFinalOfferedPrice())).thenReturn(BigDecimal.ZERO);
        when(projectEscrowRepo.save(any(ProjectEscrow.class))).thenAnswer(invocation -> {
            ProjectEscrow escrow = invocation.getArgument(0);
            escrow.setProjectEscrowId(200L);
            return escrow;
        });
        when(moneyMovementService.moneyTransactionManagement(
                eq(10L),
                eq(200L),
                eq(TransactionType.PROJECT_ESCROW_DEPOSIT),
                eq(100L),
                eq(PaymentReferenceType.PROJECT),
                eq("Wallet project escrow deposit - project 100"),
                eq(invitation.getFinalOfferedPrice()),
                eq(invitation.getFinalOfferedPrice()),
                eq(null)
        )).thenReturn(walletSuccessPayment);
        when(paymentTransactionRepo.findPendingTransactionByReferenceAndMethod(
                eq(PaymentReferenceType.INVITATION),
                eq(1L),
                eq(TransactionType.PROJECT_ESCROW_DEPOSIT),
                eq(10L),
                eq(PaymentStatus.PENDING),
                eq(PaymentMethod.SEPAY)
        )).thenReturn(Optional.of(pendingSepayPayment));

        projectPaymentService.createProjectPaymentByWallet(1L);

        assertThat(pendingSepayPayment.getPaymentStatus()).isEqualTo(PaymentStatus.EXPIRED);
        verify(paymentTransactionRepo).save(pendingSepayPayment);
        verify(notificationService).notifyProjectWorkspaceReady(any(Project.class));
    }

    private Invitation acceptedInvitation(ClientProfile clientProfile, LocalDateTime respondedAt) {
        JobPost jobPost = JobPost.builder()
                .clientProfile(clientProfile)
                .build();
        ExpertProfile expertProfile = ExpertProfile.builder()
                .expertProfileId(30L)
                .build();
        ExpertApplication expertApplication = ExpertApplication.builder()
                .jobpost(jobPost)
                .expertProfile(expertProfile)
                .build();

        return Invitation.builder()
                .invitationId(1L)
                .expertApplication(expertApplication)
                .projectTitle("AI chatbot")
                .finalRequirement("Build chatbot")
                .expectedOutput("Working chatbot")
                .acceptanceCriteria("Pass demo")
                .finalOfferedPrice(BigDecimal.valueOf(1_000_000))
                .finalTimelineValue(7)
                .finalTimelineUnit(TimelineUnit.DAY)
                .finalTimeline("7 days")
                .clientAcceptedTerms(true)
                .expertAcceptedTerms(true)
                .invitationStatus(InvitationStatus.ACCEPTED)
                .expiresAt(LocalDateTime.now().minusHours(1))
                .respondedAt(respondedAt)
                .build();
    }
}
