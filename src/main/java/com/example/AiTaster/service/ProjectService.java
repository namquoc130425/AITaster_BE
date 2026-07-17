package com.example.AiTaster.service;

import com.example.AiTaster.constant.ProjectStatus;
import com.example.AiTaster.dto.response.ProjectCardResponse;
import com.example.AiTaster.entity.ClientProfile;
import com.example.AiTaster.entity.ExpertProfile;
import com.example.AiTaster.entity.Project;
import com.example.AiTaster.entity.User;
import com.example.AiTaster.exception.GlobalException;
import com.example.AiTaster.mapper.ProjectMapper;
import com.example.AiTaster.repository.ClientProfileRepo;
import com.example.AiTaster.repository.ExpertProfileRepo;
import com.example.AiTaster.repository.ProjectMilestoneRepo;
import com.example.AiTaster.repository.ProjectRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final ProjectMapper projectMapper;
    private final RealtimeService realtimeService;

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

        return responses.stream()
                .sorted(Comparator.comparing(
                        ProjectCardResponse::getCreateAt,
                        Comparator.nullsLast(LocalDateTime::compareTo)
                ).reversed())
                .toList();
    }

    public ProjectCardResponse getProject(Long projectId) {
        User user = currentUserService.getCurrentUser();
        ClientProfile clientProfile = clientProfileRepo.findByUser(user).orElse(null);
        ExpertProfile expertProfile = expertProfileRepo.findByUser(user).orElse(null);

        if (clientProfile == null && expertProfile == null) {
            throw new GlobalException(403, "User has no client or expert profile");
        }

        Project project = projectRepo.findWithDetailByProjectId(projectId)
                .orElseThrow(() -> new GlobalException(404, "Project not found"));

        checkProjectMember(project, clientProfile, expertProfile);

        boolean isClientProject = clientProfile != null
                && isClientProject(project, clientProfile.getClientProfileId());

        return projectMapper.toCardResponse(
                project,
                projectMilestoneRepo.findByProjectId(project.getProjectId()).orElse(null),
                isClientProject
        );
    }

    @Transactional
    public void deleteProject(Long projectId) {
        Project project = projectRepo.findWithDetailByProjectId(projectId)
                .orElseThrow(() -> new GlobalException(404, "Project not found"));

        User user = currentUserService.getCurrentUser();
        ClientProfile clientProfile = clientProfileRepo.findByUser(user).orElse(null);
        ExpertProfile expertProfile = expertProfileRepo.findByUser(user).orElse(null);

        if (clientProfile == null && expertProfile == null) {
            throw new GlobalException(403, "User has no client or expert profile");
        }

        checkProjectMember(project, clientProfile, expertProfile);

        if (!canDeleteProject(project.getProjectStatus())) {
            throw new GlobalException(400, "Project in progress cannot be deleted");
        }

        markProjectDeleted(project, clientProfile, expertProfile);
        Project savedProject = projectRepo.save(project);
        realtimeService.pushProjectParticipants(
                savedProject,
                "PROJECT_DELETED",
                "Project deleted"
        );
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

    private void checkProjectMember(
            Project project,
            ClientProfile clientProfile,
            ExpertProfile expertProfile
    ) {
        boolean isClient = clientProfile != null && isClientProject(
                project,
                clientProfile.getClientProfileId()
        );

        Long projectExpertId = project.getInvitation()
                .getExpertApplication()
                .getExpertProfile()
                .getExpertProfileId();

        boolean isExpert = expertProfile != null
                && projectExpertId.equals(expertProfile.getExpertProfileId());

        if (!isClient && !isExpert) {
            throw new GlobalException(403, "You are not allowed to delete this project");
        }
    }

    private void markProjectDeleted(
            Project project,
            ClientProfile clientProfile,
            ExpertProfile expertProfile
    ) {
        if (clientProfile != null && isClientProject(
                project,
                clientProfile.getClientProfileId()
        )) {
            project.setClientDeleted(true);
        }

        Long projectExpertId = project.getInvitation()
                .getExpertApplication()
                .getExpertProfile()
                .getExpertProfileId();

        if (expertProfile != null && projectExpertId.equals(expertProfile.getExpertProfileId())) {
            project.setExpertDeleted(true);
        }
    }

    private boolean canDeleteProject(ProjectStatus status) {
        return status == ProjectStatus.COMPLETED || status == ProjectStatus.CANCELED;
    }
}
