package com.example.AiTaster.service.imp;

import com.example.AiTaster.dto.response.NotificationResponse;
import com.example.AiTaster.dto.response.UnreadNotificationCountResponse;
<<<<<<< HEAD
import com.example.AiTaster.entity.ExpertApplication;
import com.example.AiTaster.entity.Invitation;
import com.example.AiTaster.entity.Report;
import com.example.AiTaster.entity.User;
=======
import com.example.AiTaster.entity.*;
>>>>>>> 4ceb432e65237a7ca034898d24e678aac4935384

import java.util.List;

public interface INotificationService {

    NotificationResponse createAndPushNow(
            Long receiverUserId,
            String title,
            String content,
            com.example.AiTaster.constant.NotificationType notificationType,
            com.example.AiTaster.constant.ReferenceType referenceType,
            Long referenceId
    );

    void notify(
            User receiver,
            com.example.AiTaster.constant.NotificationType notificationType,
            com.example.AiTaster.constant.ReferenceType referenceType,
            Long referenceId,
            String title,
            String content
    );

    List<NotificationResponse> getMyNotifications();

    List<NotificationResponse> getMyUnreadNotifications();

    NotificationResponse markAsRead(Long notificationId);

    int markAllAsRead();

    UnreadNotificationCountResponse countMyUnreadNotifications();

    void notifyExpertApplied(ExpertApplication application);

    void notifyInvitationSent(Invitation invitation);

    void notifyInvitationAccepted(Invitation invitation);

    void notifyInvitationRejected(Invitation invitation);

    void notifyProjectWorkspaceReady(Project project);

    void notifyAdminNewReport(Report report);

    void notifyReporterReportResolved(Report report);

    void notifyReporterReportRejected(Report report);
<<<<<<< HEAD
=======

    void notifyAdminAiServiceCreated(ExpertService expertService);

    void notifyAdminAiServiceUpdated(ExpertService expertService);

    void notifyAdminAiServiceSubmitted(ExpertService expertService);

    void notifyExpertAiServiceAccepted(ExpertService expertService);

    void notifyExpertAiServiceRejected(ExpertService expertService);
}
