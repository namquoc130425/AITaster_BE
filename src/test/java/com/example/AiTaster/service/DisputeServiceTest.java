package com.example.AiTaster.service;

import com.example.AiTaster.constant.DisputeDecision;
import com.example.AiTaster.constant.DisputeStatus;
import com.example.AiTaster.constant.EscrowStatus;
import com.example.AiTaster.constant.PaymentReferenceType;
import com.example.AiTaster.constant.ProjectStatus;
import com.example.AiTaster.constant.Role;
import com.example.AiTaster.constant.TransactionType;
import com.example.AiTaster.dto.request.ResolveDisputeRequest;
import com.example.AiTaster.dto.response.DisputeResponse;
import com.example.AiTaster.entity.ClientProfile;
import com.example.AiTaster.entity.Dispute;
import com.example.AiTaster.entity.ExpertApplication;
import com.example.AiTaster.entity.ExpertProfile;
import com.example.AiTaster.entity.Invitation;
import com.example.AiTaster.entity.Invoices;
import com.example.AiTaster.entity.JobPost;
import com.example.AiTaster.entity.PaymentTransaction;
import com.example.AiTaster.entity.Project;
import com.example.AiTaster.entity.ProjectEscrow;
import com.example.AiTaster.entity.User;
import com.example.AiTaster.mapper.DeliverableMapper;
import com.example.AiTaster.mapper.DisputeMapper;
import com.example.AiTaster.mapper.InvoiceMapper;
import com.example.AiTaster.mapper.MessageMapper;
import com.example.AiTaster.mapper.ProjectMapper;
import com.example.AiTaster.repository.ConversationRepo;
import com.example.AiTaster.repository.DeliverableRepo;
import com.example.AiTaster.repository.DisputeRepo;
import com.example.AiTaster.repository.InvoicesRepo;
import com.example.AiTaster.repository.MessageRepo;
import com.example.AiTaster.repository.ProjectEscrowRepo;
import com.example.AiTaster.repository.ProjectMilestoneRepo;
import com.example.AiTaster.repository.ProjectRepo;
import com.example.AiTaster.repository.UserRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DisputeServiceTest {

    @Mock
    private DisputeRepo disputeRepo;
    @Mock
    private ProjectRepo projectRepo;
    @Mock
    private ProjectEscrowRepo projectEscrowRepo;
    @Mock
    private ProjectMilestoneRepo projectMilestoneRepo;
    @Mock
    private DeliverableRepo deliverableRepo;
    @Mock
    private UserRepo userRepo;
    @Mock
    private CurrentUserService currentUserService;
    @Mock
    private MoneyMovementService moneyMovementService;
    @Mock
    private NotificationService notificationService;
    @Mock
    private RealtimeService realtimeService;
    @Mock
    private InvoiceService invoiceService;
    @Mock
    private InvoiceEmailService invoiceEmailService;
    @Mock
    private DisputeMapper disputeMapper;
    @Mock
    private ProjectMapper projectMapper;
    @Mock
    private DeliverableMapper deliverableMapper;
    @Mock
    private InvoiceMapper invoiceMapper;
    @Mock
    private ConversationRepo conversationRepo;
    @Mock
    private MessageRepo messageRepo;
    @Mock
    private InvoicesRepo invoiceRepo;
    @Mock
    private MessageMapper messageMapper;

    @InjectMocks
    private DisputeService disputeService;

    @Test
    void resolveReleaseExpertStoresExpertNetAmountAndKeepsPlatformFee() {
        BigDecimal heldAmount = BigDecimal.valueOf(10_000);
        BigDecimal expertAmount = BigDecimal.valueOf(9_000);
        BigDecimal platformFee = BigDecimal.valueOf(1_000);

        User admin = User.builder().userId(1L).role(Role.ADMIN).build();
        User client = User.builder().userId(10L).build();
        User expert = User.builder().userId(20L).build();
        Project project = buildProject(client, expert);
        ProjectEscrow escrow = ProjectEscrow.builder()
                .projectEscrowId(40L)
                .projectId(30L)
                .heldAmount(heldAmount)
                .platformFee(platformFee)
                .expertAmount(expertAmount)
                .escrowStatus(EscrowStatus.DISPUTED)
                .build();
        Dispute dispute = Dispute.builder()
                .disputeId(50L)
                .project(project)
                .reporter(client)
                .reportedAgainst(expert)
                .reason("Need admin review")
                .disputeStatus(DisputeStatus.OPEN)
                .build();
        PaymentTransaction releasePayment = PaymentTransaction.builder()
                .paymentTransactionId(70L)
                .build();
        Invoices invoice = new Invoices();
        invoice.setInvoiceId(80L);

        when(currentUserService.getCurrentUser()).thenReturn(admin);
        when(disputeRepo.findByDisputeId(50L)).thenReturn(Optional.of(dispute));
        when(projectRepo.findWithDetailByProjectId(30L)).thenReturn(Optional.of(project));
        when(projectEscrowRepo.findByProjectIdForUpdate(30L)).thenReturn(Optional.of(escrow));
        when(moneyMovementService.calculateFee(heldAmount)).thenReturn(expertAmount);
        when(moneyMovementService.moneyTransactionManagement(
                eq(40L),
                eq(20L),
                eq(TransactionType.PROJECT_ESCROW_RELEASE),
                eq(30L),
                eq(PaymentReferenceType.PROJECT),
                anyString(),
                eq(heldAmount),
                eq(expertAmount),
                isNull()
        )).thenReturn(releasePayment);
        when(projectRepo.save(project)).thenReturn(project);
        when(projectEscrowRepo.save(escrow)).thenReturn(escrow);
        when(disputeRepo.save(dispute)).thenReturn(dispute);
        when(invoiceService.createForResolvedDispute(
                eq(30L),
                eq(70L),
                eq(BigDecimal.ZERO),
                eq(heldAmount),
                eq(expertAmount),
                eq(platformFee),
                eq(DisputeDecision.RELEASE_EXPERT)
        )).thenReturn(invoice);
        when(disputeMapper.toResponse(any(Dispute.class), eq(escrow))).thenAnswer(invocation -> {
            Dispute savedDispute = invocation.getArgument(0);
            ProjectEscrow savedEscrow = invocation.getArgument(1);

            return DisputeResponse.builder()
                    .disputeId(savedDispute.getDisputeId())
                    .refundAmount(savedDispute.getRefundAmount())
                    .releaseAmount(savedDispute.getReleaseAmount())
                    .escrowHeldAmount(savedEscrow.getHeldAmount())
                    .escrowPlatformFee(savedEscrow.getPlatformFee())
                    .escrowExpertAmount(savedEscrow.getExpertAmount())
                    .build();
        });
        when(projectMilestoneRepo.findByProjectId(30L)).thenReturn(Optional.empty());
        when(deliverableRepo.findByProjectIdOrderBySubmittedAtDesc(30L)).thenReturn(List.of());
        when(conversationRepo.findWithDetailByProjectId(30L)).thenReturn(Optional.empty());
        when(invoiceRepo.findByProjectEscrowId(40L)).thenReturn(Optional.empty());

        DisputeResponse response = disputeService.resolve(
                50L,
                new ResolveDisputeRequest(DisputeDecision.RELEASE_EXPERT, null, null, "release")
        );

        assertThat(dispute.getReleaseAmount()).isEqualByComparingTo(expertAmount);
        assertThat(dispute.getRefundAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(escrow.getPlatformFee()).isEqualByComparingTo(platformFee);
        assertThat(escrow.getExpertAmount()).isEqualByComparingTo(expertAmount);
        assertThat(response.getReleaseAmount()).isEqualByComparingTo(expertAmount);
        assertThat(response.getEscrowPlatformFee()).isEqualByComparingTo(platformFee);
        verify(invoiceEmailService).enqueueAndSendForInvoice(80L);
    }

    @Test
    void resolveSplitCreatesInvoiceWithGrossReleaseAndStoresExpertNetAmount() {
        BigDecimal heldAmount = BigDecimal.valueOf(10_000);
        BigDecimal refundAmount = BigDecimal.valueOf(4_000);
        BigDecimal releaseGrossAmount = BigDecimal.valueOf(6_000);
        BigDecimal expertNetAmount = BigDecimal.valueOf(5_400);
        BigDecimal platformFee = BigDecimal.valueOf(600);

        User admin = User.builder().userId(1L).role(Role.ADMIN).build();
        User client = User.builder().userId(10L).build();
        User expert = User.builder().userId(20L).build();
        Project project = buildProject(client, expert);
        ProjectEscrow escrow = ProjectEscrow.builder()
                .projectEscrowId(40L)
                .projectId(30L)
                .heldAmount(heldAmount)
                .platformFee(BigDecimal.ZERO)
                .expertAmount(BigDecimal.ZERO)
                .escrowStatus(EscrowStatus.DISPUTED)
                .build();
        Dispute dispute = Dispute.builder()
                .disputeId(50L)
                .project(project)
                .reporter(client)
                .reportedAgainst(expert)
                .reason("Need admin review")
                .disputeStatus(DisputeStatus.OPEN)
                .build();
        PaymentTransaction releasePayment = PaymentTransaction.builder()
                .paymentTransactionId(70L)
                .build();
        PaymentTransaction refundPayment = PaymentTransaction.builder()
                .paymentTransactionId(71L)
                .build();
        Invoices invoice = new Invoices();
        invoice.setInvoiceId(80L);

        when(currentUserService.getCurrentUser()).thenReturn(admin);
        when(disputeRepo.findByDisputeId(50L)).thenReturn(Optional.of(dispute));
        when(projectRepo.findWithDetailByProjectId(30L)).thenReturn(Optional.of(project));
        when(projectEscrowRepo.findByProjectIdForUpdate(30L)).thenReturn(Optional.of(escrow));
        when(moneyMovementService.calculateFee(releaseGrossAmount)).thenReturn(expertNetAmount);
        when(moneyMovementService.moneyTransactionManagement(
                eq(40L),
                eq(20L),
                eq(TransactionType.PROJECT_ESCROW_RELEASE),
                eq(30L),
                eq(PaymentReferenceType.PROJECT),
                anyString(),
                eq(releaseGrossAmount),
                eq(expertNetAmount),
                isNull()
        )).thenReturn(releasePayment);
        when(moneyMovementService.moneyTransactionManagement(
                eq(40L),
                eq(10L),
                eq(TransactionType.PROJECT_ESCROW_REFUND),
                eq(30L),
                eq(PaymentReferenceType.PROJECT),
                anyString(),
                eq(refundAmount),
                eq(refundAmount),
                isNull()
        )).thenReturn(refundPayment);
        when(projectRepo.save(project)).thenReturn(project);
        when(projectEscrowRepo.save(escrow)).thenReturn(escrow);
        when(disputeRepo.save(dispute)).thenReturn(dispute);
        when(invoiceService.createForResolvedDispute(
                eq(30L),
                eq(70L),
                eq(refundAmount),
                eq(releaseGrossAmount),
                eq(expertNetAmount),
                eq(platformFee),
                eq(DisputeDecision.SPLIT)
        )).thenReturn(invoice);
        when(disputeMapper.toResponse(any(Dispute.class), eq(escrow))).thenAnswer(invocation -> {
            Dispute savedDispute = invocation.getArgument(0);
            ProjectEscrow savedEscrow = invocation.getArgument(1);

            return DisputeResponse.builder()
                    .disputeId(savedDispute.getDisputeId())
                    .refundAmount(savedDispute.getRefundAmount())
                    .releaseAmount(savedDispute.getReleaseAmount())
                    .escrowHeldAmount(savedEscrow.getHeldAmount())
                    .escrowPlatformFee(savedEscrow.getPlatformFee())
                    .escrowExpertAmount(savedEscrow.getExpertAmount())
                    .build();
        });
        when(projectMilestoneRepo.findByProjectId(30L)).thenReturn(Optional.empty());
        when(deliverableRepo.findByProjectIdOrderBySubmittedAtDesc(30L)).thenReturn(List.of());
        when(conversationRepo.findWithDetailByProjectId(30L)).thenReturn(Optional.empty());
        when(invoiceRepo.findByProjectEscrowId(40L)).thenReturn(Optional.empty());

        DisputeResponse response = disputeService.resolve(
                50L,
                new ResolveDisputeRequest(
                        DisputeDecision.SPLIT,
                        refundAmount,
                        releaseGrossAmount,
                        "split"
                )
        );

        assertThat(dispute.getRefundAmount()).isEqualByComparingTo(refundAmount);
        assertThat(dispute.getReleaseAmount()).isEqualByComparingTo(expertNetAmount);
        assertThat(escrow.getPlatformFee()).isEqualByComparingTo(platformFee);
        assertThat(escrow.getExpertAmount()).isEqualByComparingTo(expertNetAmount);
        assertThat(response.getReleaseAmount()).isEqualByComparingTo(expertNetAmount);
        assertThat(response.getEscrowPlatformFee()).isEqualByComparingTo(platformFee);
        verify(invoiceEmailService).enqueueAndSendForInvoice(80L);
    }

    private Project buildProject(User client, User expert) {
        ClientProfile clientProfile = ClientProfile.builder()
                .clientProfileId(11L)
                .user(client)
                .build();
        JobPost jobPost = JobPost.builder()
                .clientProfile(clientProfile)
                .build();
        ExpertProfile expertProfile = ExpertProfile.builder()
                .expertProfileId(21L)
                .user(expert)
                .build();
        ExpertApplication application = ExpertApplication.builder()
                .jobpost(jobPost)
                .expertProfile(expertProfile)
                .build();
        Invitation invitation = Invitation.builder()
                .expertApplication(application)
                .build();

        return Project.builder()
                .projectId(30L)
                .title("AI dispute project")
                .invitation(invitation)
                .projectStatus(ProjectStatus.ACTIVE)
                .isActive(true)
                .build();
    }
}
