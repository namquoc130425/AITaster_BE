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
                safeText(jobPost.getTitle(), "job post của bạn");

        createAndSend(
                clientUser,
                NotificationCreateRequest.builder()
                        .title("Có expert mới ứng tuyển")
                        .content(expertName + " đã ứng tuyển vào job post: " + jobTitle)
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
                safeText(invitation.getProjectTitle(), "dự án mới");

        createAndSend(
                expertUser,
                NotificationCreateRequest.builder()
                        .title("Bạn nhận được lời mời dự án")
                        .content("Client đã gửi cho bạn lời mời tham gia dự án: " + projectTitle)
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
                safeText(invitation.getProjectTitle(), "dự án");

        createAndSend(
                clientUser,
                NotificationCreateRequest.builder()
                        .title("Expert đã chấp nhận lời mời")
                        .content(expertName + " đã chấp nhận lời mời dự án: " + projectTitle)
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
                safeText(invitation.getProjectTitle(), "dự án");

        createAndSend(
                clientUser,
                NotificationCreateRequest.builder()
                        .title("Expert đã từ chối lời mời")
                        .content(expertName + " đã từ chối lời mời dự án: " + projectTitle)
                        .notificationType(NotificationType.INVITATION)
                        .referenceType(ReferenceType.INVITATION)
                        .referenceId(invitation.getInvitationId())
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
                            .title("Có report mới")
                            .content(reporterName + " đã gửi một report mới: " + report.getReportTitle())
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
                        .title("Report của bạn đã được xử lý")
                        .content("Admin đã xử lý report: " + report.getReportTitle())
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
                        .title("Report của bạn đã bị từ chối")
                        .content("Admin đã từ chối report: " + report.getReportTitle())
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

    private String safeText(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }

        return value;
    }
}