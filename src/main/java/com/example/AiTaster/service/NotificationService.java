package com.example.AiTaster.service;

import com.example.AiTaster.constant.NotificationType;
import com.example.AiTaster.constant.ReferenceType;
import com.example.AiTaster.constant.Role;
import com.example.AiTaster.constant.ErrorCode;
import com.example.AiTaster.dto.response.NotificationResponse;
import com.example.AiTaster.dto.response.UnreadNotificationCountResponse;
import com.example.AiTaster.entity.*;
import com.example.AiTaster.event.NotificationCreatedEvent;
import com.example.AiTaster.exception.GlobalException;
import com.example.AiTaster.mapper.NotificationMapper;
import com.example.AiTaster.repository.NotificationRepo;
import com.example.AiTaster.repository.UserRepo;
import com.example.AiTaster.service.imp.INotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService implements INotificationService {

    private final NotificationRepo notificationRepo;
    private final UserRepo userRepo;
    private final NotificationMapper notificationMapper;
    private final CurrentUserService currentUserService;
    private final SimpMessagingTemplate messagingTemplate;
    private final ApplicationEventPublisher eventPublisher;

    /*
     * Dùng cho trường hợp cần tạo notification ngay.
     * Thông thường service nghiệp vụ KHÔNG gọi trực tiếp hàm này.
     * Service nghiệp vụ nên gọi notify...() để notification chạy sau khi commit.
     */
    @Override
    @Transactional
    public NotificationResponse createAndPushNow(
            Long receiverUserId,
            String title,
            String content,
            NotificationType notificationType,
            ReferenceType referenceType,
            Long referenceId
    ) {
        User receiver =
                userRepo.findById(receiverUserId)
                        .orElseThrow(() ->
                                new GlobalException(ErrorCode.USER_NOT_FOUND)
                        );

        Notification notification =
                Notification.builder()
                        .user(receiver)
                        .title(title)
                        .content(content)
                        .notificationType(notificationType)
                        .referenceType(referenceType)
                        .referenceId(referenceId)
                        .isRead(false)
                        .build();

        Notification saved =
                notificationRepo.save(notification);

        NotificationResponse response =
                notificationMapper.toResponse(saved);

        pushToUser(receiver, response);

        return response;
    }

    /*
     * Hàm phát notification dùng chung.
     * Đây là API có thể tái sử dụng cho những service nghiệp vụ sau này.
     */
    @Override
    public void notify(
            User receiver,
            NotificationType notificationType,
            ReferenceType referenceType,
            Long referenceId,
            String title,
            String content
    ) {
        if (receiver == null || receiver.getUserId() == null) {
            return;
        }

        eventPublisher.publishEvent(
                NotificationCreatedEvent.builder()
                        .receiverUserId(receiver.getUserId())
                        .title(title)
                        .content(content)
                        .notificationType(notificationType)
                        .referenceType(referenceType)
                        .referenceId(referenceId)
                        .build()
        );
    }

    /*
     * Sau khi transaction của nghiệp vụ chính commit thành công,
     * hàm này mới chạy.
     *
     * Nếu nghiệp vụ chính rollback,
     * notification sẽ không được tạo và không bị push ảo lên FE.
     */
    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT,
            fallbackExecution = true
    )
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleNotificationCreatedEvent(
            NotificationCreatedEvent event
    ) {
        createAndPushNow(
                event.getReceiverUserId(),
                event.getTitle(),
                event.getContent(),
                event.getNotificationType(),
                event.getReferenceType(),
                event.getReferenceId()
        );
    }

    /*
     * Đẩy notification riêng tư theo user destination.
     *
     * FE lắng nghe:
     * /user/queue/notifications
     *
     * Vì WebSocket Principal hiện tại là UsernamePasswordAuthenticationToken,
     * Spring dùng principal.getName().
     * Với UserDetails, getName() thường trả về username.
     *
     * Nên ở đây gửi theo receiver.getUsername().
     */
    private void pushToUser(
            User receiver,
            NotificationResponse response
    ) {
        if (receiver.getUsername() == null || receiver.getUsername().isBlank()) {
            return;
        }

        messagingTemplate.convertAndSendToUser(
                receiver.getUsername(),
                "/queue/notifications",
                response
        );

        /*
     * Tương thích ngược không bắt buộc:
     * Nếu FE cũ vẫn lắng nghe /topic/users/{id}/notifications
         * thì vẫn nhận được.
         *
         * Sau khi FE chuyển hẳn sang /user/queue/notifications,
         * có thể xóa đoạn này.
         */
        messagingTemplate.convertAndSend(
                "/topic/users/" + receiver.getUserId() + "/notifications",
                response
        );
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

    /*
     * Trường hợp 1:
     * Expert ứng tuyển job post.
     *
     * Người nhận: client sở hữu JobPost.
     */
    @Override
    public void notifyExpertApplied(
            ExpertApplication application
    ) {
        if (application == null
                || application.getJobpost() == null
                || application.getJobpost().getClientProfile() == null
                || application.getJobpost().getClientProfile().getUser() == null
                || application.getExpertProfile() == null
                || application.getExpertProfile().getUser() == null) {
            return;
        }

        User clientUser =
                application.getJobpost()
                        .getClientProfile()
                        .getUser();

        User expertUser =
                application.getExpertProfile()
                        .getUser();

        String expertName =
                safeName(expertUser);

        String jobTitle =
                safeText(
                        application.getJobpost().getTitle(),
                        "job post của bạn"
                );

        notify(
                clientUser,
                NotificationType.APPLICATION,
                ReferenceType.APPLICATION,
                application.getApplicationId(),
                "Có expert mới ứng tuyển",
                expertName + " đã ứng tuyển vào job post: " + jobTitle
        );
    }

    /*
     * Trường hợp 2:
     * Client gửi invitation.
     *
     * Người nhận: expert được mời.
     *
     * Đây là trường hợp FE đang thiếu:
     * client thao tác nhưng expert không biết.
     */
    @Override
    public void notifyInvitationSent(
            Invitation invitation
    ) {
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
                safeText(
                        invitation.getProjectTitle(),
                        "dự án mới"
                );

        notify(
                expertUser,
                NotificationType.INVITATION,
                ReferenceType.INVITATION,
                invitation.getInvitationId(),
                "Bạn nhận được lời mời dự án",
                "Client đã gửi cho bạn lời mời tham gia dự án: " + projectTitle
        );
    }

    /*
     * Trường hợp 3:
     * Expert chấp nhận invitation.
     *
     * Người nhận: client.
     */
    @Override
    public void notifyInvitationAccepted(
            Invitation invitation
    ) {
        if (invitation == null
                || invitation.getExpertApplication() == null
                || invitation.getExpertApplication().getJobpost() == null
                || invitation.getExpertApplication().getJobpost().getClientProfile() == null
                || invitation.getExpertApplication().getJobpost().getClientProfile().getUser() == null
                || invitation.getExpertApplication().getExpertProfile() == null
                || invitation.getExpertApplication().getExpertProfile().getUser() == null) {
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
                safeText(
                        invitation.getProjectTitle(),
                        "dự án"
                );

        notify(
                clientUser,
                NotificationType.INVITATION,
                ReferenceType.INVITATION,
                invitation.getInvitationId(),
                "Expert đã chấp nhận lời mời",
                expertName + " đã chấp nhận lời mời dự án: " + projectTitle
        );
    }

    /*
     * Trường hợp 4:
     * Expert từ chối invitation.
     *
     * Người nhận: client.
     */
    @Override
    public void notifyInvitationRejected(
            Invitation invitation
    ) {
        if (invitation == null
                || invitation.getExpertApplication() == null
                || invitation.getExpertApplication().getJobpost() == null
                || invitation.getExpertApplication().getJobpost().getClientProfile() == null
                || invitation.getExpertApplication().getJobpost().getClientProfile().getUser() == null
                || invitation.getExpertApplication().getExpertProfile() == null
                || invitation.getExpertApplication().getExpertProfile().getUser() == null) {
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
                safeText(
                        invitation.getProjectTitle(),
                        "dự án"
                );

        notify(
                clientUser,
                NotificationType.INVITATION,
                ReferenceType.INVITATION,
                invitation.getInvitationId(),
                "Expert đã từ chối lời mời",
                expertName + " đã từ chối lời mời dự án: " + projectTitle
        );
    }

    @Override
    public void notifyProjectWorkspaceReady(Project project) {
        if (project == null
                || project.getProjectId() == null
                || project.getInvitation() == null
                || project.getInvitation().getExpertApplication() == null
                || project.getInvitation().getExpertApplication().getJobpost() == null
                || project.getInvitation().getExpertApplication().getJobpost().getClientProfile() == null
                || project.getInvitation().getExpertApplication().getJobpost().getClientProfile().getUser() == null
                || project.getInvitation().getExpertApplication().getExpertProfile() == null
                || project.getInvitation().getExpertApplication().getExpertProfile().getUser() == null) {
            return;
        }

        User clientUser = project.getInvitation()
                .getExpertApplication()
                .getJobpost()
                .getClientProfile()
                .getUser();

        User expertUser = project.getInvitation()
                .getExpertApplication()
                .getExpertProfile()
                .getUser();

        String projectTitle = safeText(project.getTitle(), "dự án");
        String title = "Project workspace is ready";
        String content = "Escrow payment completed. Open workspace for: " + projectTitle;

        notify(
                clientUser,
                NotificationType.PROJECT,
                ReferenceType.PROJECT,
                project.getProjectId(),
                title,
                content
        );
        notify(
                expertUser,
                NotificationType.PROJECT,
                ReferenceType.PROJECT,
                project.getProjectId(),
                title,
                content
        );
    }

    /*
     * Report:
     * User tạo report -> admin nhận notification.
     */
    @Override
    public void notifyAdminNewReport(
            Report report
    ) {
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
            notify(
                    admin,
                    NotificationType.REPORT,
                    ReferenceType.REPORT,
                    report.getReportId(),
                    "Có report mới",
                    reporterName + " đã gửi một report mới: " + report.getReportTitle()
            );
        }
    }

    /*
     * Report:
     * Admin xử lý report -> người gửi report nhận notification.
     */
    @Override
    public void notifyReporterReportResolved(
            Report report
    ) {
        if (report == null || report.getReporter() == null) {
            return;
        }

        notify(
                report.getReporter(),
                NotificationType.REPORT,
                ReferenceType.REPORT,
                report.getReportId(),
                "Report của bạn đã được xử lý",
                safeText(
                        report.getAdminResponse(),
                        "Admin đã xử lý report: " + report.getReportTitle()
                )
        );
    }

    /*
     * Report:
     * Admin từ chối report -> người gửi report nhận notification.
     */
    @Override
    public void notifyReporterReportRejected(
            Report report
    ) {
        if (report == null || report.getReporter() == null) {
            return;
        }

        notify(
                report.getReporter(),
                NotificationType.REPORT,
                ReferenceType.REPORT,
                report.getReportId(),
                "Report của bạn đã bị từ chối",
                safeText(
                        report.getAdminResponse(),
                        "Admin đã từ chối report: " + report.getReportTitle()
                )
        );
    }

<<<<<<< HEAD
=======
    @Override
    public void notifyAdminAiServiceCreated(ExpertService expertService) {
        if (expertService == null) {
            return;
        }

        notifyAllAdmins(
                NotificationType.EXPERT_SERVICE,
                ReferenceType.EXPERT_SERVICE,
                expertService.getServiceId(),
                "AI Service mới được tạo",
                "Expert vừa tạo AI Service '" + expertService.getServiceName()
                        + "'. Trạng thái hiện tại là DRAFT."
        );
    }

    @Override
    public void notifyAdminAiServiceUpdated(ExpertService expertService) {
        if (expertService == null) {
            return;
        }

        notifyAllAdmins(
                NotificationType.EXPERT_SERVICE,
                ReferenceType.EXPERT_SERVICE,
                expertService.getServiceId(),
                "AI Service vừa được cập nhật",
                "Expert vừa cập nhật AI Service '" + expertService.getServiceName()
                        + "'. Service cần được kiểm tra nếu được submit lại."
        );
    }

    @Override
    public void notifyAdminAiServiceSubmitted(ExpertService expertService) {
        if (expertService == null) {
            return;
        }

        notifyAllAdmins(
                NotificationType.EXPERT_SERVICE,
                ReferenceType.EXPERT_SERVICE,
                expertService.getServiceId(),
                "AI Service chờ duyệt",
                "Expert đã gửi AI Service '" + expertService.getServiceName()
                        + "' để admin duyệt."
        );
    }

    @Override
    public void notifyExpertAiServiceAccepted(ExpertService expertService) {
        User expertUser = getExpertOwner(expertService);

        if (expertUser == null) {
            return;
        }

        notify(
                expertUser,
                NotificationType.EXPERT_SERVICE,
                ReferenceType.EXPERT_SERVICE,
                expertService.getServiceId(),
                "AI Service đã được duyệt",
                "AI Service '" + expertService.getServiceName()
                        + "' đã được admin duyệt và đang hiển thị công khai."
        );
    }

    @Override
    public void notifyExpertAiServiceRejected(ExpertService expertService) {
        User expertUser = getExpertOwner(expertService);

        if (expertUser == null) {
            return;
        }

        String reason = expertService.getRejectionReason();

        if (reason == null || reason.isBlank()) {
            reason = "Admin đã từ chối AI Service của bạn. Vui lòng kiểm tra lại nội dung và resubmit.";
        }

        notify(
                expertUser,
                NotificationType.EXPERT_SERVICE,
                ReferenceType.EXPERT_SERVICE,
                expertService.getServiceId(),
                "AI Service bị từ chối",
                "AI Service '" + expertService.getServiceName()
                        + "' bị từ chối. Lý do: " + reason
        );
    }

    private void notifyAllAdmins(
            NotificationType notificationType,
            ReferenceType referenceType,
            Long referenceId,
            String title,
            String content
    ) {
        List<User> admins = userRepo.findByRole(Role.ADMIN);

        for (User admin : admins) {
            notify(
                    admin,
                    notificationType,
                    referenceType,
                    referenceId,
                    title,
                    content
            );
        }
    }

    private User getExpertOwner(ExpertService expertService) {
        if (expertService == null
                || expertService.getExpertProfile() == null
                || expertService.getExpertProfile().getUser() == null) {
            return null;
        }

        return expertService
                .getExpertProfile()
                .getUser();
    }

>>>>>>> 4ceb432e65237a7ca034898d24e678aac4935384
    private Notification getNotification(
            Long notificationId
    ) {
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

    private String safeName(
            User user
    ) {
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

    private String safeText(
            String value,
            String fallback
    ) {
        if (value == null || value.isBlank()) {
            return fallback;
        }

        return value;
    }
}
