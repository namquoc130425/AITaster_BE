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
import com.example.AiTaster.mapper.DisputeMapper;
import com.example.AiTaster.repository.*;
import com.example.AiTaster.service.imp.IDisputeService;
import com.example.AiTaster.specification.DisputeSpecification;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DisputeService implements IDisputeService {
    private final DisputeRepo disputeRepo;
    private final ProjectRepo projectRepo;
    private final ProjectEscrowRepo projectEscrowRepo;
    private final DeliverableRepo deliverableRepo;
    private final UserRepo userRepo;
    private final CurrentUserService currentUserService;
    private final MoneyMovementService moneyMovementService;
    private final NotificationService notificationService;
    private final RealtimeService realtimeService;
    private final DisputeMapper disputeMapper;

    @Override
    @Transactional
    public DisputeResponse create(Long projectId, CreateDisputeRequest request) {
        User currentUser = currentUserService.getCurrentUser();

        Project project = getProject(projectId);
        checkParticipant(project, currentUser);

        if (project.getProjectStatus() != ProjectStatus.ACTIVE) {
            throw new GlobalException(400, "Only active project can be disputed");
        }

        if (disputeRepo.existsByProject_ProjectIdAndDisputeStatusIn(
                projectId,
                List.of(DisputeStatus.OPEN, DisputeStatus.UNDER_REVIEW)
        )) {
            throw new GlobalException(400, "Project already has an open dispute");
        }

        ProjectEscrow escrow = getEscrowForUpdate(projectId);
        if (escrow.getEscrowStatus() != EscrowStatus.HELD) {
            throw new GlobalException(400, "Escrow is not held");
        }

        Deliverable deliverable = null;
        if (request.getDeliverableId() != null) {
            deliverable = deliverableRepo.findById(request.getDeliverableId())
                    .orElseThrow(() -> new GlobalException(404, "Deliverable not found"));

            if (!projectId.equals(deliverable.getProjectId())) {
                throw new GlobalException(400, "Deliverable does not belong to project");
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
                "Project has a new dispute",
                displayName(currentUser) + " opened a dispute for project: " + project.getTitle()
        );

        realtimeService.pushProjectParticipants(project, "PROJECT_DISPUTED", "Project has been disputed");
        realtimeService.pushAdminDisputeEvent("DISPUTE_CREATED", dispute.getDisputeId(), "New dispute created");

        return disputeMapper.toResponse(dispute, escrow);
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

            return disputeMapper.toResponse(dispute, escrow);
        });

        return PageResponse.fromPage(responsePage);
    }

    @Override
    @Transactional
    public DisputeResponse resolve(Long disputeId, ResolveDisputeRequest request) {
        checkAdmin();

        Dispute dispute = disputeRepo.findByDisputeId(disputeId)
                .orElseThrow(() -> new GlobalException(404, "Dispute not found"));

        if (dispute.getDisputeStatus() == DisputeStatus.RESOLVED
                || dispute.getDisputeStatus() == DisputeStatus.REJECTED) {
            throw new GlobalException(400, "Dispute already closed");
        }

        Project project = getProject(dispute.getProject().getProjectId());
        ProjectEscrow escrow = getEscrowForUpdate(project.getProjectId());

        if (escrow.getEscrowStatus() != EscrowStatus.DISPUTED) {
            throw new GlobalException(400, "Escrow is not under dispute");
        }

        BigDecimal held = escrow.getHeldAmount();

        if (held == null || held.compareTo(BigDecimal.ZERO) <= 0) {
            throw new GlobalException(400, "Escrow held amount is invalid");
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
                validateNonNegative(refund, "Refund amount");
                validateNonNegative(release, "Release amount");

                if (refund.add(release).compareTo(held) != 0) {
                    throw new GlobalException(400, "Refund + release must equal held amount");
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
                        saved.getDisputeId(), "Dispute rejected", "Admin rejected the dispute for project: " + project.getTitle());
                notificationService.notify(expertUser, NotificationType.DISPUTE, ReferenceType.DISPUTE,
                        saved.getDisputeId(), "Dispute rejected", "Admin rejected the dispute for project: " + project.getTitle());
                realtimeService.pushProjectParticipants(project, "DISPUTE_REJECTED", "Dispute was rejected by admin");
                realtimeService.pushAdminDisputeEvent("DISPUTE_REJECTED", saved.getDisputeId(), "Dispute rejected");
                return disputeMapper.toResponse(saved, escrow);
            }
        }

        if (release.compareTo(BigDecimal.ZERO) > 0) {
            moneyMovementService.moneyTransactionManagement(
                    escrow.getProjectEscrowId(),
                    expertUser.getUserId(),
                    TransactionType.PROJECT_ESCROW_RELEASE,
                    project.getProjectId(),
                    PaymentReferenceType.PROJECT,
                    "Dispute release to expert - project " + project.getProjectId(),
                    release,
                    release,
                    null
            );
        }

        if (refund.compareTo(BigDecimal.ZERO) > 0) {
            moneyMovementService.moneyTransactionManagement(
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
        dispute.setReleaseAmount(release);
        dispute.setResponse(request.getResponse());
        dispute.setResolvedAt(LocalDateTime.now());

        escrow.setHeldAmount(BigDecimal.ZERO);
        escrow.setEscrowStatus(resolveEscrowStatus(refund, release));
        project.setIsActive(false);
        project.setProjectStatus(refund.compareTo(BigDecimal.ZERO) == 0
                ? ProjectStatus.COMPLETED
                : ProjectStatus.CANCELED);

        projectRepo.save(project);
        projectEscrowRepo.save(escrow);

        Dispute saved = disputeRepo.save(dispute);

        notificationService.notify(clientUser, NotificationType.DISPUTE, ReferenceType.DISPUTE,
                saved.getDisputeId(), "Dispute resolved", "Admin resolved dispute for project: " + project.getTitle());

        notificationService.notify(expertUser, NotificationType.DISPUTE, ReferenceType.DISPUTE,
                saved.getDisputeId(), "Dispute resolved", "Admin resolved dispute for project: " + project.getTitle());

        realtimeService.pushProjectParticipants(project, "DISPUTE_RESOLVED", "Dispute resolved by admin");
        realtimeService.pushAdminDisputeEvent("DISPUTE_RESOLVED", saved.getDisputeId(), "Dispute resolved");

        return disputeMapper.toResponse(saved, escrow);
    }

    private Project getProject(Long projectId) {
        return projectRepo.findWithDetailByProjectId(projectId)
                .orElseThrow(() -> new GlobalException(404, "Project not found"));
    }

    private ProjectEscrow getEscrowForUpdate(Long projectId) {
        return projectEscrowRepo.findByProjectIdForUpdate(projectId)
                .orElseThrow(() -> new GlobalException(404, "Project escrow not found"));
    }

    private void checkAdmin() {
        User user = currentUserService.getCurrentUser();
        if (user.getRole() != Role.ADMIN) {
            throw new GlobalException(403, "Only admin can use this API");
        }
    }

    private void checkParticipant(Project project, User user) {
        Long userId = user.getUserId();
        if (!userId.equals(getClientUser(project).getUserId())
                && !userId.equals(getExpertUser(project).getUserId())) {
            throw new GlobalException(403, "You are not a participant of this project");
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
                    "New dispute",
                    displayName(dispute.getReporter()) + " opened a dispute for project: "
                            + dispute.getProject().getTitle()
            );
        }
    }

    private BigDecimal safe(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private void validateNonNegative(BigDecimal amount, String fieldName) {
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new GlobalException(400, fieldName + " must not be negative");
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
            return "User";
        }

        if (user.getFullName() != null && !user.getFullName().isBlank()) {
            return user.getFullName();
        }

        if (user.getUsername() != null && !user.getUsername().isBlank()) {
            return user.getUsername();
        }

        return "User";
    }
}
