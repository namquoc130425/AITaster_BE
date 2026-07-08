package com.example.AiTaster.service.imp;

import com.example.AiTaster.dto.response.NotificationResponse;
import com.example.AiTaster.dto.response.UnreadNotificationCountResponse;
import com.example.AiTaster.entity.*;

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

    void notifyAdminNewReport(Report report);

    void notifyReporterReportResolved(Report report);

    void notifyReporterReportRejected(Report report);

    void notifyAdminAiServiceCreated(ExpertService expertService);

    void notifyAdminAiServiceUpdated(ExpertService expertService);

    void notifyAdminAiServiceSubmitted(ExpertService expertService);

    void notifyExpertAiServiceAccepted(ExpertService expertService);

    void notifyExpertAiServiceRejected(ExpertService expertService);
}