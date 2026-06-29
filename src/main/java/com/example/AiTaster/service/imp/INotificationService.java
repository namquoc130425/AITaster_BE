package com.example.AiTaster.service.imp;

import com.example.AiTaster.dto.request.NotificationCreateRequest;
import com.example.AiTaster.dto.response.NotificationResponse;
import com.example.AiTaster.dto.response.UnreadNotificationCountResponse;
import com.example.AiTaster.entity.ExpertApplication;
import com.example.AiTaster.entity.ExpertProposal;
import com.example.AiTaster.entity.ExpertService;
import com.example.AiTaster.entity.Invitation;
import com.example.AiTaster.entity.ClientProfile;
import com.example.AiTaster.entity.Project;
import com.example.AiTaster.entity.User;

import java.util.List;

public interface INotificationService {

    NotificationResponse createAndSend(
            User receiver,
            NotificationCreateRequest request
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

    void notifyProjectCompleted(Project project);

    void notifyProposalPurchased(ExpertProposal proposal, ClientProfile clientProfile);

    void notifyExpertServicePurchased(ExpertService expertService, ClientProfile clientProfile);

    void notifyAdminNewReport(com.example.AiTaster.entity.Report report);

    void notifyReporterReportResolved(com.example.AiTaster.entity.Report report);

    void notifyReporterReportRejected(com.example.AiTaster.entity.Report report);
}
