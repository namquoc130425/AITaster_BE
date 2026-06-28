package com.example.AiTaster.service;

import com.example.AiTaster.constant.InvitationStatus;
import com.example.AiTaster.dto.response.ProjectCardResponse;
import com.example.AiTaster.entity.ClientProfile;
import com.example.AiTaster.entity.ExpertProfile;
import com.example.AiTaster.entity.Invitation;
import com.example.AiTaster.entity.Project;
import com.example.AiTaster.entity.User;
import com.example.AiTaster.exception.GlobalException;
import com.example.AiTaster.mapper.ProjectMapper;
import com.example.AiTaster.repository.ClientProfileRepo;
import com.example.AiTaster.repository.ExpertProfileRepo;
import com.example.AiTaster.repository.InvitationRepo;
import com.example.AiTaster.repository.ProjectMilestoneRepo;
import com.example.AiTaster.repository.ProjectRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectService {
    private final CurrentUserService currentUserService;
    private final ClientProfileRepo clientProfileRepo;
    private final ExpertProfileRepo expertProfileRepo;
    private final ProjectRepo projectRepo;
    private final ProjectMilestoneRepo projectMilestoneRepo;
    private final InvitationRepo invitationRepo;
    private final ProjectMapper projectMapper;

    public List<ProjectCardResponse> getMyProjects(String search) {
        User user = currentUserService.getCurrentUser();
        ClientProfile clientProfile = clientProfileRepo.findByUser(user).orElse(null);
        ExpertProfile expertProfile = expertProfileRepo.findByUser(user).orElse(null);

        if (clientProfile == null && expertProfile == null) {
            throw new GlobalException(403, "User has no client or expert profile");
        }

        Long clientProfileId = clientProfile != null ? clientProfile.getClientProfileId() : null;
        Long expertProfileId = expertProfile != null ? expertProfile.getExpertProfileId() : null;
        String keyword = search == null ? "" : search.trim();

        List<ProjectCardResponse> responses = new ArrayList<>();

        List<Project> projects = projectRepo.findMyProjects(clientProfileId, expertProfileId, keyword);
        responses.addAll(projects.stream()
                .map(project -> projectMapper.toCardResponse(
                        project,
                        projectMilestoneRepo.findByProjectId(project.getProjectId()).orElse(null),
                        isClientProject(project, clientProfileId)
                ))
                .toList());

        List<Invitation> invitations = invitationRepo.findMyProjectInvitationsWithoutProject(
                clientProfileId,
                expertProfileId,
                List.of(
                        InvitationStatus.PENDING,
                        InvitationStatus.ACCEPTED,
                        InvitationStatus.PAYMENT_EXPIRED
                ),
                keyword
        );
        responses.addAll(invitations.stream()
                .map(invitation -> projectMapper.toInvitationCardResponse(
                        invitation,
                        isClientInvitation(invitation, clientProfileId)
                ))
                .toList());

        return responses.stream()
                .sorted(Comparator.comparing(
                        ProjectCardResponse::getCreateAt,
                        Comparator.nullsLast(LocalDateTime::compareTo)
                ).reversed())
                .toList();
    }

    private boolean isClientProject(Project project, Long clientProfileId) {
        if (clientProfileId == null) {
            return false;
        }

        Long ownerClientId = project.getInvitation()
                .getExpertApplication()
                .getJobpost()
                .getClientProfile()
                .getClientProfileId();

        return clientProfileId.equals(ownerClientId);
    }

    private boolean isClientInvitation(Invitation invitation, Long clientProfileId) {
        if (clientProfileId == null) {
            return false;
        }

        Long ownerClientId = invitation
                .getExpertApplication()
                .getJobpost()
                .getClientProfile()
                .getClientProfileId();

        return clientProfileId.equals(ownerClientId);
    }
}
