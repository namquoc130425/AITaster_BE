package com.example.AiTaster.service;


import com.example.AiTaster.Util.PageUtil;
import com.example.AiTaster.constant.*;
import com.example.AiTaster.dto.request.CreateDisputeRequest;
import com.example.AiTaster.dto.request.DisputeFilterRequest;
import com.example.AiTaster.dto.request.ResolveDisputeRequest;
import com.example.AiTaster.dto.response.DisputeResponse;
import com.example.AiTaster.dto.response.PageResponse;
import com.example.AiTaster.entity.*;
import com.example.AiTaster.exception.GlobalException;
import com.example.AiTaster.mapper.DeliverableMapper;
import com.example.AiTaster.mapper.DisputeMapper;
import com.example.AiTaster.mapper.InvoiceMapper;
import com.example.AiTaster.mapper.MessageMapper;
import com.example.AiTaster.mapper.ProjectMapper;
import com.example.AiTaster.repository.*;
import com.example.AiTaster.service.imp.IDisputeService;
import com.example.AiTaster.specification.DisputeSpecification;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DisputeService implements IDisputeService {
    private final DisputeRepo disputeRepo;
    private final ProjectRepo projectRepo;
    private final ProjectEscrowRepo projectEscrowRepo;
    private final ProjectMilestoneRepo projectMilestoneRepo;
    private final DeliverableRepo deliverableRepo;
    private final UserRepo userRepo;
    private final CurrentUserService currentUserService;
    private final MoneyMovementService moneyMovementService;
    private final NotificationService notificationService;
    private final RealtimeService realtimeService;
    private final InvoiceService invoiceService;
    private final InvoiceEmailService invoiceEmailService;
    private final DisputeMapper disputeMapper;
    private final ProjectMapper projectMapper;
    private final DeliverableMapper deliverableMapper;
    private final InvoiceMapper invoiceMapper;
    private final ConversationRepo conversationRepo;
    private final MessageRepo messageRepo;
    private final InvoicesRepo invoiceRepo;
    private final MessageMapper messageMapper;

    @Override
    @Transactional
    public DisputeResponse create(Long projectId, CreateDisputeRequest request) {
        User currentUser = currentUserService.getCurrentUser();

        Project project = getProject(projectId);
        checkParticipant(project, currentUser);

        if (project.getProjectStatus() != ProjectStatus.ACTIVE) {
            throw new GlobalException(400, "Chỉ dự án đang hoạt động mới có thể mở tranh chấp");
        }

        if (disputeRepo.existsByProject_ProjectIdAndDisputeStatusIn(
                projectId,
                List.of(DisputeStatus.OPEN, DisputeStatus.UNDER_REVIEW)
        )) {
            throw new GlobalException(400, "Dự án đã có một tranh chấp đang mở");
        }

        ProjectEscrow escrow = getEscrowForUpdate(projectId);
        if (escrow.getEscrowStatus() != EscrowStatus.HELD) {
            throw new GlobalException(400, "Khoản ký quỹ không ở trạng thái đang giữ");
        }

        Deliverable deliverable = null;
        if (request.getDeliverableId() != null) {
            deliverable = deliverableRepo.findById(request.getDeliverableId())
                    .orElseThrow(() -> new GlobalException(404, "Không tìm thấy sản phẩm bàn giao"));

            if (!projectId.equals(deliverable.getProjectId())) {
                throw new GlobalException(400, "Tệp bàn giao không thuộc dự án này");
            }
        }

        User reportedAgainst = getOtherParticipant(project, currentUser);

        Dispute dispute = disputeRepo.save(Dispute.builder()
                .project(project)
                .deliverable(deliverable)
                .reporter(currentUser)
                .reportedAgainst(reportedAgainst)
                .reason(request.getReason())
                .evidence(request.getEvidence())
                .build());

        project.setProjectStatus(ProjectStatus.DISPUTED);
        project.setIsActive(false);
        escrow.setEscrowStatus(EscrowStatus.DISPUTED);

        projectRepo.save(project);
        projectEscrowRepo.save(escrow);

        notifyAdmins(dispute);
        notificationService.notify(
                reportedAgainst,
                NotificationType.DISPUTE,
                ReferenceType.DISPUTE,
                dispute.getDisputeId(),
                "Dự án có tranh chấp mới",
                displayName(currentUser) + " đã mở tranh chấp cho dự án: " + project.getTitle()
        );

        pushAfterCommit(() -> {
            realtimeService.pushProjectParticipants(project, "PROJECT_DISPUTED", "Dự án đã phát sinh tranh chấp");
            realtimeService.pushAdminDisputeEvent(
                    "DISPUTE_CREATED",
                    dispute.getDisputeId(),
                    project.getProjectId(),
                    "Đã tạo tranh chấp mới"
            );
        });

        return toResponse(dispute, escrow, currentUser);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<DisputeResponse> filterAdmin(DisputeFilterRequest request) {
        checkAdmin();

        if (request == null) {
            request = new DisputeFilterRequest();
        }

        Page<Dispute> page = disputeRepo.findAll(
                DisputeSpecification.filter(request),
                PageUtil.createPageable(request)
        );

        Page<DisputeResponse> responsePage = page.map(dispute -> {
            ProjectEscrow escrow = projectEscrowRepo
                    .findByProjectId(dispute.getProject().getProjectId())
                    .orElse(null);

            return toResponse(dispute, escrow, null);
        });

        return PageResponse.fromPage(responsePage);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DisputeResponse> getMyDisputes() {
        User currentUser = currentUserService.getCurrentUser();

        return disputeRepo
                .findByReporter_UserIdOrReportedAgainst_UserIdOrderByCreateAtDesc(
                        currentUser.getUserId(),
                        currentUser.getUserId()
                )
                .stream()
                .map(dispute -> {
                    ProjectEscrow escrow = projectEscrowRepo
                            .findByProjectId(dispute.getProject().getProjectId())
                            .orElse(null);

                    return toResponse(dispute, escrow, currentUser);
                })
                .toList();
    }

    @Override
    @Transactional
    public DisputeResponse resolve(Long disputeId, ResolveDisputeRequest request) {
        checkAdmin();

        Dispute dispute = disputeRepo.findByDisputeId(disputeId)
                .orElseThrow(() -> new GlobalException(404, "Không tìm thấy tranh chấp"));

        if (dispute.getDisputeStatus() == DisputeStatus.RESOLVED
                || dispute.getDisputeStatus() == DisputeStatus.REJECTED) {
            throw new GlobalException(400, "Tranh chấp đã được đóng");
        }

        Project project = getProject(dispute.getProject().getProjectId());
        ProjectEscrow escrow = getEscrowForUpdate(project.getProjectId());

        if (escrow.getEscrowStatus() != EscrowStatus.DISPUTED) {
            throw new GlobalException(400, "Khoản ký quỹ không ở trạng thái tranh chấp");
        }

        BigDecimal held = escrow.getHeldAmount();

        if (held == null || held.compareTo(BigDecimal.ZERO) <= 0) {
            throw new GlobalException(400, "Số tiền đang giữ trong ký quỹ không hợp lệ");
        }

        User clientUser = getClientUser(project);
        User expertUser = getExpertUser(project);

        BigDecimal refund = BigDecimal.ZERO;
        BigDecimal release = BigDecimal.ZERO;

        switch (request.getDecision()) {
            case REFUND_CLIENT -> refund = held;
            case RELEASE_EXPERT -> release = held;
            case SPLIT -> {
                refund = safe(request.getRefundAmount());
                release = safe(request.getReleaseAmount());
                validateNonNegative(refund, "Số tiền hoàn lại");
                validateNonNegative(release, "Số tiền giải ngân");

                if (refund.add(release).compareTo(held) != 0) {
                    throw new GlobalException(400, "Tổng số tiền hoàn lại và giải ngân phải bằng số tiền đang giữ");
                }
            }
            case REJECT -> {
                dispute.setDisputeStatus(DisputeStatus.REJECTED);
                dispute.setDisputeDecision(DisputeDecision.REJECT);
                dispute.setResponse(request.getResponse());
                dispute.setResolvedAt(LocalDateTime.now());

                project.setProjectStatus(ProjectStatus.ACTIVE);
                project.setIsActive(true);
                escrow.setEscrowStatus(EscrowStatus.HELD);

                projectRepo.save(project);
                projectEscrowRepo.save(escrow);

                Dispute saved = disputeRepo.save(dispute);
                notificationService.notify(clientUser, NotificationType.DISPUTE, ReferenceType.DISPUTE,
                        saved.getDisputeId(), "Tranh chấp đã bị từ chối", "Quản trị viên đã từ chối tranh chấp của dự án: " + project.getTitle());
                notificationService.notify(expertUser, NotificationType.DISPUTE, ReferenceType.DISPUTE,
                        saved.getDisputeId(), "Tranh chấp đã bị từ chối", "Quản trị viên đã từ chối tranh chấp của dự án: " + project.getTitle());
                pushAfterCommit(() -> {
                    realtimeService.pushProjectParticipants(project, "DISPUTE_REJECTED", "Tranh chấp đã bị quản trị viên từ chối");
                    realtimeService.pushAdminDisputeEvent(
                            "DISPUTE_REJECTED",
                            saved.getDisputeId(),
                            project.getProjectId(),
                            "Tranh chấp đã bị từ chối"
                    );
                });
                return toResponse(saved, escrow, null);
            }
        }

        PaymentTransaction releasePayment = null;
        PaymentTransaction refundPayment = null;
        BigDecimal resolvedPlatformFee = BigDecimal.ZERO;
        BigDecimal resolvedExpertAmount = BigDecimal.ZERO;

        if (release.compareTo(BigDecimal.ZERO) > 0) {
            resolvedExpertAmount = moneyMovementService.calculateFee(release);
            resolvedPlatformFee = release.subtract(resolvedExpertAmount);

            releasePayment = moneyMovementService.moneyTransactionManagement(
                    escrow.getProjectEscrowId(),
                    expertUser.getUserId(),
                    TransactionType.PROJECT_ESCROW_RELEASE,
                    project.getProjectId(),
                    PaymentReferenceType.PROJECT,
                    "Dispute release to expert - project " + project.getProjectId(),
                    release,
                    resolvedExpertAmount,
                    null
            );
        }

        if (refund.compareTo(BigDecimal.ZERO) > 0) {
            refundPayment = moneyMovementService.moneyTransactionManagement(
                    escrow.getProjectEscrowId(),
                    clientUser.getUserId(),
                    TransactionType.PROJECT_ESCROW_REFUND,
                    project.getProjectId(),
                    PaymentReferenceType.PROJECT,
                    "Dispute refund to client - project " + project.getProjectId(),
                    refund,
                    refund,
                    null
            );
        }

        dispute.setDisputeStatus(DisputeStatus.RESOLVED);
        dispute.setDisputeDecision(request.getDecision());
        dispute.setRefundAmount(refund);
        dispute.setReleaseAmount(resolvedExpertAmount);
        dispute.setResponse(request.getResponse());
        dispute.setResolvedAt(LocalDateTime.now());

        escrow.setHeldAmount(BigDecimal.ZERO);
        escrow.setPlatformFee(resolvedPlatformFee);
        escrow.setExpertAmount(resolvedExpertAmount);
        escrow.setEscrowStatus(resolveEscrowStatus(refund, release));
        project.setIsActive(false);
        project.setProjectStatus(ProjectStatus.CANCELED);

        projectRepo.save(project);
        projectEscrowRepo.save(escrow);

        Dispute saved = disputeRepo.save(dispute);

        Long invoicePaymentId = releasePayment != null
                ? releasePayment.getPaymentTransactionId()
                : refundPayment != null
                ? refundPayment.getPaymentTransactionId()
                : null;

        if (invoicePaymentId != null) {
            Invoices invoice = invoiceService.createForResolvedDispute(
                    project.getProjectId(),
                    invoicePaymentId,
                    refund,
                    release,
                    resolvedExpertAmount,
                    resolvedPlatformFee,
                    request.getDecision()
            );
            pushAfterCommit(() -> invoiceEmailService.enqueueAndSendForInvoice(invoice.getInvoiceId()));
        }

        notificationService.notify(clientUser, NotificationType.DISPUTE, ReferenceType.DISPUTE,
                saved.getDisputeId(), "Tranh chấp đã được xử lý", "Quản trị viên đã xử lý tranh chấp của dự án: " + project.getTitle());

        notificationService.notify(expertUser, NotificationType.DISPUTE, ReferenceType.DISPUTE,
                saved.getDisputeId(), "Tranh chấp đã được xử lý", "Quản trị viên đã xử lý tranh chấp của dự án: " + project.getTitle());

        pushAfterCommit(() -> {
            realtimeService.pushProjectParticipants(project, "DISPUTE_RESOLVED", "Tranh chấp đã được quản trị viên xử lý");
            realtimeService.pushAdminDisputeEvent(
                    "DISPUTE_RESOLVED",
                    saved.getDisputeId(),
                    project.getProjectId(),
                    "Tranh chấp đã được xử lý"
            );
        });

        return toResponse(saved, escrow, null);
    }

    private DisputeResponse toResponse(Dispute dispute, ProjectEscrow escrow, User viewer) {
        DisputeResponse response = disputeMapper.toResponse(dispute, escrow);
        Project project = dispute.getProject();

        if (escrow != null) {
            response.setEscrowStatus(escrow.getEscrowStatus());
            if (dispute.getDisputeStatus() == DisputeStatus.RESOLVED) {
                invoiceRepo.findByProjectEscrowId(escrow.getProjectEscrowId())
                        .ifPresent(invoice -> response.setInvoice(invoiceMapper.toInvoiceResponse(invoice)));
            }
        }

        if (project != null && project.getProjectId() != null) {
            boolean isClientProject = viewer == null
                    || viewer.getUserId().equals(getClientUser(project).getUserId());

            response.setProject(projectMapper.toCardResponse(
                    project,
                    projectMilestoneRepo.findByProjectId(project.getProjectId()).orElse(null),
                    isClientProject
            ));
            response.setDeliverables(deliverableRepo
                    .findByProjectIdOrderBySubmittedAtDesc(project.getProjectId())
                    .stream()
                    .map(deliverableMapper::toResponse)
                    .toList());
            conversationRepo.findWithDetailByProjectId(project.getProjectId())
                    .ifPresent(conversation -> {
                        response.setConversationId(conversation.getConversationId());
                        response.setMessages(messageRepo
                                .findByConversationOrderBySendAtAsc(conversation)
                                .stream()
                                .map(messageMapper::toResponse)
                                .toList());
                    });
        }

        response.setProjectOutcome(resolveProjectOutcome(dispute));
        return response;
    }

    private String resolveProjectOutcome(Dispute dispute) {
        if (dispute.getDisputeStatus() == DisputeStatus.REJECTED) {
            return "CONTINUE_PROJECT";
        }

        if (dispute.getDisputeStatus() == DisputeStatus.RESOLVED) {
            return "CLOSE_PROJECT";
        }

        return "PENDING_ADMIN_REVIEW";
    }

    private void pushAfterCommit(Runnable action) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            action.run();
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                action.run();
            }
        });
    }

    private Project getProject(Long projectId) {
        return projectRepo.findWithDetailByProjectId(projectId)
                .orElseThrow(() -> new GlobalException(404, "Không tìm thấy dự án"));
    }

    private ProjectEscrow getEscrowForUpdate(Long projectId) {
        return projectEscrowRepo.findByProjectIdForUpdate(projectId)
                .orElseThrow(() -> new GlobalException(404, "Không tìm thấy khoản ký quỹ của dự án"));
    }

    private void checkAdmin() {
        User user = currentUserService.getCurrentUser();
        if (!Role.ADMIN.equals(user.getRole())) {
            throw new GlobalException(403, "Chỉ quản trị viên mới có thể thực hiện thao tác này");
        }
    }

    private void checkParticipant(Project project, User user) {
        Long userId = user.getUserId();
        if (!userId.equals(getClientUser(project).getUserId())
                && !userId.equals(getExpertUser(project).getUserId())) {
            throw new GlobalException(403, "Bạn không phải thành viên của dự án này");
        }
    }

    private User getOtherParticipant(Project project, User currentUser) {
        User client = getClientUser(project);
        User expert = getExpertUser(project);

        return currentUser.getUserId().equals(client.getUserId()) ? expert : client;
    }

    private User getClientUser(Project project) {
        return project.getInvitation()
                .getExpertApplication()
                .getJobpost()
                .getClientProfile()
                .getUser();
    }

    private User getExpertUser(Project project) {
        return project.getInvitation()
                .getExpertApplication()
                .getExpertProfile()
                .getUser();
    }

    private void notifyAdmins(Dispute dispute) {
        for (User admin : userRepo.findByRole(Role.ADMIN)) {
            notificationService.notify(
                    admin,
                    NotificationType.DISPUTE,
                    ReferenceType.DISPUTE,
                    dispute.getDisputeId(),
                    "Có tranh chấp mới",
                    displayName(dispute.getReporter()) + " đã mở tranh chấp cho dự án: "
                            + dispute.getProject().getTitle()
            );
        }
    }

    private BigDecimal safe(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private void validateNonNegative(BigDecimal amount, String fieldName) {
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new GlobalException(400, fieldName + " không được là số âm");
        }
    }

    private EscrowStatus resolveEscrowStatus(BigDecimal refund, BigDecimal release) {
        if (refund.compareTo(BigDecimal.ZERO) > 0 && release.compareTo(BigDecimal.ZERO) > 0) {
            return EscrowStatus.RESOLVED;
        }

        if (release.compareTo(BigDecimal.ZERO) > 0) {
            return EscrowStatus.RELEASED;
        }

        return EscrowStatus.REFUNDED;
    }

    private String displayName(User user) {
        if (user == null) {
            return "Người dùng";
        }

        if (user.getFullName() != null && !user.getFullName().isBlank()) {
            return user.getFullName();
        }

        if (user.getUsername() != null && !user.getUsername().isBlank()) {
            return user.getUsername();
        }

        return "Người dùng";
    }
}
