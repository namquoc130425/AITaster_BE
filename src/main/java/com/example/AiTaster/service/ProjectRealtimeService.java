package com.example.AiTaster.service;

import com.example.AiTaster.dto.response.ProjectRealtimeEventResponse;
import com.example.AiTaster.entity.Invitation;
import com.example.AiTaster.entity.Project;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ProjectRealtimeService {
    private final SimpMessagingTemplate messagingTemplate;

    public void invitationAccepted(Invitation invitation) {
        if (invitation == null || invitation.getExpertApplication() == null) {
            return;
        }

        Long clientUserId = invitation.getExpertApplication()
                .getJobpost()
                .getClientProfile()
                .getUser()
                .getUserId();

        ProjectRealtimeEventResponse event = ProjectRealtimeEventResponse.builder()
                .eventType("INVITATION_ACCEPTED")
                .invitationId(invitation.getInvitationId())
                .jobPostId(invitation.getExpertApplication().getJobpost().getJobPostId())
                .targetUserId(clientUserId)
                .message("Expert accepted your invitation. Escrow payment is ready.")
                .at(LocalDateTime.now())
                .build();

        messagingTemplate.convertAndSend("/topic/users/" + clientUserId + "/projects", event);
    }

    public void projectCompleted(Project project) {
        if (project == null || project.getInvitation() == null || project.getInvitation().getExpertApplication() == null) {
            return;
        }

        Long clientUserId = project.getInvitation().getExpertApplication().getJobpost().getClientProfile().getUser().getUserId();
        Long expertUserId = project.getInvitation().getExpertApplication().getExpertProfile().getUser().getUserId();

        ProjectRealtimeEventResponse event = ProjectRealtimeEventResponse.builder()
                .eventType("PROJECT_COMPLETED")
                .projectId(project.getProjectId())
                .invitationId(project.getInvitation().getInvitationId())
                .jobPostId(project.getInvitation().getExpertApplication().getJobpost().getJobPostId())
                .message("Project completed and escrow released.")
                .at(LocalDateTime.now())
                .build();

        messagingTemplate.convertAndSend("/topic/projects/" + project.getProjectId(), event);
        messagingTemplate.convertAndSend("/topic/users/" + clientUserId + "/projects", event);
        messagingTemplate.convertAndSend("/topic/users/" + expertUserId + "/projects", event);
    }
}
