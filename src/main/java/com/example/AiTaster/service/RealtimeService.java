package com.example.AiTaster.service;

import com.example.AiTaster.constant.ReferenceType;
import com.example.AiTaster.constant.Role;
import com.example.AiTaster.dto.response.MessageResponse;
import com.example.AiTaster.dto.response.RealtimeEventResponse;
import com.example.AiTaster.entity.Invitation;
import com.example.AiTaster.entity.Project;
import com.example.AiTaster.entity.User;
import com.example.AiTaster.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RealtimeService {

    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepo userRepo;

    public void pushUserDashboardEvent(
            User user,
            String eventType,
            ReferenceType referenceType,
            Long referenceId,
            String message
    ) {
        RealtimeEventResponse event = buildEvent(
                user,
                eventType,
                referenceType,
                referenceId,
                null,
                null,
                null,
                message
        );

        sendToUser(user, "/queue/dashboard", event);
        sendLegacyUserTopic(user, "dashboard", event);
    }

    public void pushUserMessage(
            User user,
            MessageResponse message
    ) {
        sendToUser(user, "/queue/messages", message);
        sendLegacyUserTopic(user, "messages", message);

        pushUserDashboardEvent(
                user,
                "MESSAGE_RECEIVED",
                ReferenceType.CONVERSATION,
                message.getConversationId(),
                "Có tin nhắn mới"
        );
    }

    public void pushUserWalletEvent(
            User user,
            String eventType,
            Long walletId,
            String message
    ) {
        RealtimeEventResponse event = buildEvent(
                user,
                eventType,
                ReferenceType.WITHDRAW,
                walletId,
                null,
                walletId,
                null,
                message
        );

        sendToUser(user, "/queue/wallet", event);
        sendLegacyUserTopic(user, "wallet", event);
        pushUserDashboardEvent(
                user,
                eventType,
                ReferenceType.WITHDRAW,
                walletId,
                message
        );
    }

    public void pushUserProjectEvent(
            User user,
            String eventType,
            Long projectId,
            String message
    ) {
        RealtimeEventResponse event = buildEvent(
                user,
                eventType,
                ReferenceType.PROJECT,
                projectId,
                projectId,
                null,
                null,
                message
        );

        sendToUser(user, "/queue/projects", event);
        sendLegacyUserTopic(user, "projects", event);
        pushUserDashboardEvent(
                user,
                eventType,
                ReferenceType.PROJECT,
                projectId,
                message
        );
    }

    public void pushProjectParticipants(
            Project project,
            String eventType,
            String message
    ) {
        User clientUser = getClientUser(project);
        User expertUser = getExpertUser(project);
        Long projectId = project == null ? null : project.getProjectId();

        pushUserProjectEvent(clientUser, eventType, projectId, message);
        pushUserProjectEvent(expertUser, eventType, projectId, message);
    }

    public void pushInvitationParticipants(
            Invitation invitation,
            String eventType,
            String message
    ) {
        User clientUser = getClientUser(invitation);
        User expertUser = getExpertUser(invitation);
        Long invitationId = invitation == null ? null : invitation.getInvitationId();

        RealtimeEventResponse clientEvent = buildEvent(
                clientUser,
                eventType,
                ReferenceType.INVITATION,
                invitationId,
                null,
                null,
                null,
                message
        );
        RealtimeEventResponse expertEvent = buildEvent(
                expertUser,
                eventType,
                ReferenceType.INVITATION,
                invitationId,
                null,
                null,
                null,
                message
        );

        sendToUser(clientUser, "/queue/projects", clientEvent);
        sendToUser(expertUser, "/queue/projects", expertEvent);
        sendLegacyUserTopic(clientUser, "projects", clientEvent);
        sendLegacyUserTopic(expertUser, "projects", expertEvent);
        pushUserDashboardEvent(
                clientUser,
                eventType,
                ReferenceType.INVITATION,
                invitationId,
                message
        );
        pushUserDashboardEvent(
                expertUser,
                eventType,
                ReferenceType.INVITATION,
                invitationId,
                message
        );
    }

    public void pushAdminWithdrawalEvent(
            String eventType,
            Long walletId,
            String message
    ) {
        RealtimeEventResponse event = RealtimeEventResponse.builder()
                .eventType(eventType)
                .referenceType(ReferenceType.WITHDRAW)
                .referenceId(walletId)
                .walletId(walletId)
                .message(message)
                .at(LocalDateTime.now())
                .build();

        messagingTemplate.convertAndSend("/topic/admin/withdrawals", event);

        userRepo.findByRole(Role.ADMIN)
                .forEach(admin -> sendToUser(admin, "/queue/dashboard", event));
    }

    private RealtimeEventResponse buildEvent(
            User user,
            String eventType,
            ReferenceType referenceType,
            Long referenceId,
            Long projectId,
            Long walletId,
            Long conversationId,
            String message
    ) {
        return RealtimeEventResponse.builder()
                .eventType(eventType)
                .referenceType(referenceType)
                .referenceId(referenceId)
                .userId(user == null ? null : user.getUserId())
                .projectId(projectId)
                .walletId(walletId)
                .conversationId(conversationId)
                .message(message)
                .at(LocalDateTime.now())
                .build();
    }

    private void sendToUser(
            User user,
            String destination,
            Object payload
    ) {
        if (user == null || user.getUsername() == null || user.getUsername().isBlank()) {
            return;
        }

        messagingTemplate.convertAndSendToUser(
                user.getUsername(),
                destination,
                payload
        );
    }

    private void sendLegacyUserTopic(
            User user,
            String channel,
            Object payload
    ) {
        if (user == null || user.getUserId() == null) {
            return;
        }

        messagingTemplate.convertAndSend(
                "/topic/users/" + user.getUserId() + "/" + channel,
                payload
        );
    }

    private User getClientUser(Project project) {
        if (project == null
                || project.getInvitation() == null
                || project.getInvitation().getExpertApplication() == null
                || project.getInvitation().getExpertApplication().getJobpost() == null
                || project.getInvitation().getExpertApplication().getJobpost().getClientProfile() == null) {
            return null;
        }

        return project.getInvitation()
                .getExpertApplication()
                .getJobpost()
                .getClientProfile()
                .getUser();
    }

    private User getExpertUser(Project project) {
        if (project == null
                || project.getInvitation() == null
                || project.getInvitation().getExpertApplication() == null
                || project.getInvitation().getExpertApplication().getExpertProfile() == null) {
            return null;
        }

        return project.getInvitation()
                .getExpertApplication()
                .getExpertProfile()
                .getUser();
    }

    private User getClientUser(Invitation invitation) {
        if (invitation == null
                || invitation.getExpertApplication() == null
                || invitation.getExpertApplication().getJobpost() == null
                || invitation.getExpertApplication().getJobpost().getClientProfile() == null) {
            return null;
        }

        return invitation.getExpertApplication()
                .getJobpost()
                .getClientProfile()
                .getUser();
    }

    private User getExpertUser(Invitation invitation) {
        if (invitation == null
                || invitation.getExpertApplication() == null
                || invitation.getExpertApplication().getExpertProfile() == null) {
            return null;
        }

        return invitation.getExpertApplication()
                .getExpertProfile()
                .getUser();
    }
}
