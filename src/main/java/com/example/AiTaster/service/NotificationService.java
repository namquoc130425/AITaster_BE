package com.example.AiTaster.service;

import com.example.AiTaster.constant.ErrorCode;
import com.example.AiTaster.constant.NotificationType;
import com.example.AiTaster.constant.ReferenceType;
import com.example.AiTaster.constant.Role;
import com.example.AiTaster.dto.request.NotificationCreateRequest;
import com.example.AiTaster.dto.response.NotificationResponse;
import com.example.AiTaster.dto.response.UnreadNotificationCountResponse;
import com.example.AiTaster.entity.*;
import com.example.AiTaster.exception.GlobalException;
import com.example.AiTaster.mapper.NotificationMapper;
import com.example.AiTaster.repository.NotificationRepo;
import com.example.AiTaster.repository.UserRepo;
import com.example.AiTaster.service.imp.INotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService implements INotificationService {

    private final NotificationRepo notificationRepo;
    private final NotificationMapper notificationMapper;
    private final CurrentUserService currentUserService;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepo userRepo;

    @Override
    @Transactional
    public NotificationResponse createAndSend(
            User receiver,
            NotificationCreateRequest request
    ) {
        if (receiver == null) {
            throw new GlobalException(ErrorCode.USER_NOT_FOUND);
        }

        if (request == null) {
            throw new GlobalException(ErrorCode.FIELD_REQUIRED);
        }

        Notification notification =
                notificationMapper.toEntity(request, receiver);

        Notification saved =
                notificationRepo.save(notification);

        NotificationResponse response =
                notificationMapper.toResponse(saved);

        pushNotification(receiver.getUserId(), response);

        return response;
    }

    @Override
    public List<NotificationResponse> getMyNotifications() {
        User currentUser =
                currentUserService.getCurrentUser();

        return notificationRepo
                .findByUserOrderByCreatedAtDesc(currentUser)
                .stream()
                .map(notificationMapper::toResponse)
                .toList();
    }

    @Override
    public List<NotificationResponse> getMyUnreadNotifications() {
        User currentUser =
                currentUserService.getCurrentUser();

        return notificationRepo
                .findByUserAndIsReadFalseOrderByCreatedAtDesc(currentUser)
                .stream()
                .map(notificationMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public NotificationResponse markAsRead(Long notificationId) {
        User currentUser =
                currentUserService.getCurrentUser();

        Notification notification =
                getNotification(notificationId);

        checkOwner(notification, currentUser);

        notification.setIsRead(true);

        Notification saved =
                notificationRepo.save(notification);

        return notificationMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public int markAllAsRead() {
        User currentUser =
                currentUserService.getCurrentUser();

        return notificationRepo.markAllAsReadByUser(currentUser);
    }

    @Override
    public UnreadNotificationCountResponse countMyUnreadNotifications() {
        User currentUser =
                currentUserService.getCurrentUser();

        long count =
                notificationRepo.countByUserAndIsReadFalse(currentUser);

        return UnreadNotificationCountResponse.builder()
                .unreadCount(count)
                .build();
    }

    @Override
    @Transactional
    public void notifyExpertApplied(ExpertApplication application) {
        if (application == null) {
            return;
        }

        JobPost jobPost =
                application.getJobpost();

        ExpertProfile expertProfile =
                application.getExpertProfile();

        if (jobPost == null
                || jobPost.getClientProfile() == null
                || jobPost.getClientProfile().getUser() == null
                || expertProfile == null
                || expertProfile.getUser() == null) {
            return;
        }

        User clientUser =
                jobPost.getClientProfile().getUser();

        String expertName =
                safeName(expertProfile.getUser());

        String jobTitle =
                safeText(jobPost.getTitle(), "your job post");

        createAndSend(
                clientUser,
                NotificationCreateRequest.builder()
                        .title("New expert application")
                        .content(expertName + " applied to job post: " + jobTitle)
                        .notificationType(NotificationType.APPLICATION)
                        .referenceType(ReferenceType.APPLICATION)
                        .referenceId(application.getApplicationId())
                        .build()
        );
    }

    @Override
    @Transactional
    public void notifyInvitationSent(Invitation invitation) {
        if (invitation == null
                || invitation.getExpertApplication() == null
                || invitation.getExpertApplication().getExpertProfile() == null
                || invitation.getExpertApplication().getExpertProfile().getUser() == null) {
            return;
        }

        User expertUser =
                invitation.getExpertApplication()
                        .getExpertProfile()
                        .getUser();

        String projectTitle =
                safeText(invitation.getProjectTitle(), "project");

        createAndSend(
                expertUser,
                NotificationCreateRequest.builder()
                        .title("New project invitation")
                        .content("A client invited you to join project: " + projectTitle)
                        .notificationType(NotificationType.INVITATION)
                        .referenceType(ReferenceType.INVITATION)
                        .referenceId(invitation.getInvitationId())
                        .build()
        );
    }

    @Override
    @Transactional
    public void notifyInvitationAccepted(Invitation invitation) {
        if (invitation == null
                || invitation.getExpertApplication() == null
                || invitation.getExpertApplication().getJobpost() == null
                || invitation.getExpertApplication().getJobpost().getClientProfile() == null
                || invitation.getExpertApplication().getJobpost().getClientProfile().getUser() == null) {
            return;
        }

        User clientUser =
                invitation.getExpertApplication()
                        .getJobpost()
                        .getClientProfile()
                        .getUser();

        User expertUser =
                invitation.getExpertApplication()
                        .getExpertProfile()
                        .getUser();

        String expertName =
                safeName(expertUser);

        String projectTitle =
                safeText(invitation.getProjectTitle(), "project");

        createAndSend(
                clientUser,
                NotificationCreateRequest.builder()
                        .title("Expert accepted your invitation")
                        .content(expertName + " accepted the project invitation: " + projectTitle)
                        .notificationType(NotificationType.INVITATION)
                        .referenceType(ReferenceType.INVITATION)
                        .referenceId(invitation.getInvitationId())
                        .build()
        );
    }

    @Override
    @Transactional
    public void notifyInvitationRejected(Invitation invitation) {
        if (invitation == null
                || invitation.getExpertApplication() == null
                || invitation.getExpertApplication().getJobpost() == null
                || invitation.getExpertApplication().getJobpost().getClientProfile() == null
                || invitation.getExpertApplication().getJobpost().getClientProfile().getUser() == null) {
            return;
        }

        User clientUser =
                invitation.getExpertApplication()
                        .getJobpost()
                        .getClientProfile()
                        .getUser();

        User expertUser =
                invitation.getExpertApplication()
                        .getExpertProfile()
                        .getUser();

        String expertName =
                safeName(expertUser);

        String projectTitle =
                safeText(invitation.getProjectTitle(), "project");

        createAndSend(
                clientUser,
                NotificationCreateRequest.builder()
                        .title("Expert rejected your invitation")
                        .content(expertName + " rejected the project invitation: " + projectTitle)
                        .notificationType(NotificationType.INVITATION)
                        .referenceType(ReferenceType.INVITATION)
                        .referenceId(invitation.getInvitationId())
                        .build()
        );
    }

    @Override
    @Transactional
    public void notifyProjectCompleted(Project project) {
        if (project == null
                || project.getInvitation() == null
                || project.getInvitation().getExpertApplication() == null) {
            return;
        }

        ExpertApplication application = project.getInvitation().getExpertApplication();
        User clientUser = application.getJobpost().getClientProfile().getUser();
        User expertUser = application.getExpertProfile().getUser();

        String title = safeText(project.getTitle(), "Project");
        NotificationCreateRequest request = NotificationCreateRequest.builder()
                .title("Project completed")
                .content(title + " is completed. Escrow has been released and files remain available in Projects.")
                .notificationType(NotificationType.PROJECT)
                .referenceType(ReferenceType.PROJECT)
                .referenceId(project.getProjectId())
                .build();

        createAndSend(clientUser, request);
        createAndSend(expertUser, request);
    }

    @Override
    @Transactional
    public void notifyProposalPurchased(ExpertProposal proposal, ClientProfile clientProfile) {
        if (proposal == null
                || proposal.getExpertApplication() == null
                || proposal.getExpertApplication().getExpertProfile() == null
                || proposal.getExpertApplication().getExpertProfile().getUser() == null
                || clientProfile == null
                || clientProfile.getUser() == null) {
            return;
        }

        User expertUser = proposal.getExpertApplication().getExpertProfile().getUser();
        String clientName = safeName(clientProfile.getUser());
        String proposalTitle = safeText(proposal.getTitle(), "your proposal");

        createAndSend(
                expertUser,
                NotificationCreateRequest.builder()
                        .title("Proposal purchased")
                        .content(clientName + " purchased proposal: " + proposalTitle)
                        .notificationType(NotificationType.ESCROW)
                        .referenceType(ReferenceType.APPLICATION)
                        .referenceId(proposal.getExpertApplication().getApplicationId())
                        .build()
        );
    }

    @Override
    @Transactional
    public void notifyExpertServicePurchased(ExpertService expertService, ClientProfile clientProfile) {
        if (expertService == null
                || expertService.getExpertProfile() == null
                || expertService.getExpertProfile().getUser() == null
                || clientProfile == null
                || clientProfile.getUser() == null) {
            return;
        }

        User expertUser = expertService.getExpertProfile().getUser();
        String clientName = safeName(clientProfile.getUser());
        String serviceName = safeText(expertService.getServiceName(), "your AI service");

        createAndSend(
                expertUser,
                NotificationCreateRequest.builder()
                        .title("AI service purchased")
                        .content(clientName + " purchased AI service: " + serviceName)
                        .notificationType(NotificationType.ESCROW)
                        .referenceType(ReferenceType.NONE)
                        .referenceId(expertService.getServiceId())
                        .build()
        );
    }

    @Override
    @Transactional
    public void notifyAdminNewReport(Report report) {
        if (report == null || report.getReporter() == null) {
            return;
        }

        List<User> admins =
                userRepo.findByRole(Role.ADMIN);

        if (admins == null || admins.isEmpty()) {
            return;
        }

        String reporterName =
                safeName(report.getReporter());

        for (User admin : admins) {
            createAndSend(
                    admin,
                    NotificationCreateRequest.builder()
                            .title("CÃ³ report má»›i")
                            .content(reporterName + " Ä‘Ã£ gá»­i má»™t report má»›i: " + report.getReportTitle())
                            .title("New report")
                            .content(reporterName + " submitted a new report: " + report.getReportTitle())
                            .notificationType(NotificationType.REPORT)
                            .referenceType(ReferenceType.REPORT)
                            .referenceId(report.getReportId())
                            .build()
            );
        }
    }

    @Override
    @Transactional
    public void notifyReporterReportResolved(Report report) {
        if (report == null || report.getReporter() == null) {
            return;
        }

        createAndSend(
                report.getReporter(),
                NotificationCreateRequest.builder()
                        .title("Report cá»§a báº¡n Ä‘Ã£ Ä‘Æ°á»£c xá»­ lÃ½")
                        .content("Admin Ä‘Ã£ xá»­ lÃ½ report: " + report.getReportTitle())
                        .title("Your report was resolved")
                        .content("Admin resolved report: " + report.getReportTitle())
                        .notificationType(NotificationType.REPORT)
                        .referenceType(ReferenceType.REPORT)
                        .referenceId(report.getReportId())
                        .build()
        );
    }

    @Override
    @Transactional
    public void notifyReporterReportRejected(Report report) {
        if (report == null || report.getReporter() == null) {
            return;
        }

        createAndSend(
                report.getReporter(),
                NotificationCreateRequest.builder()
                        .title("Report cá»§a báº¡n Ä‘Ã£ bá»‹ tá»« chá»‘i")
                        .content("Admin Ä‘Ã£ tá»« chá»‘i report: " + report.getReportTitle())
                        .title("Your report was rejected")
                        .content("Admin rejected report: " + report.getReportTitle())
                        .notificationType(NotificationType.REPORT)
                        .referenceType(ReferenceType.REPORT)
                        .referenceId(report.getReportId())
                        .build()
        );
    }

    private void pushNotification(
            Long userId,
            NotificationResponse response
    ) {
        messagingTemplate.convertAndSend(
                "/topic/users/" + userId + "/notifications",
                response
        );
    }

    private Notification getNotification(Long notificationId) {
        return notificationRepo.findByNotificationId(notificationId)
                .orElseThrow(() ->
                        new GlobalException(ErrorCode.NOTIFICATION_NOT_FOUND)
                );
    }

    private void checkOwner(
            Notification notification,
            User currentUser
    ) {
        if (!notification.getUser()
                .getUserId()
                .equals(currentUser.getUserId())) {
            throw new GlobalException(ErrorCode.NOT_NOTIFICATION_OWNER);
        }
    }

    private String safeName(User user) {
        if (user == null) {
            return "NgÆ°á»i dÃ¹ng";
        }

        if (user.getFullName() != null && !user.getFullName().isBlank()) {
            return user.getFullName();
        }

        if (user.getUsername() != null && !user.getUsername().isBlank()) {
            return user.getUsername();
        }

        return "NgÆ°á»i dÃ¹ng";
    }

    private String safeText(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }

        return value;
    }
}
