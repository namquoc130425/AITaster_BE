package com.example.AiTaster.repository;

import com.example.AiTaster.entity.Invitation;
import com.example.AiTaster.entity.Project;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProjectRepo extends JpaRepository<Project, Long> {

    boolean existsByInvitation(Invitation invitation);

    Optional<Project> findByProjectId(Long projectId);

    Optional<Project> findByInvitation(Invitation invitation);

    @EntityGraph(attributePaths = {
            "invitation",
            "invitation.expertApplication",
            "invitation.expertApplication.jobpost",
            "invitation.expertApplication.jobpost.clientProfile",
            "invitation.expertApplication.jobpost.clientProfile.user",
            "invitation.expertApplication.expertProfile",
            "invitation.expertApplication.expertProfile.user"
    })
    Optional<Project> findWithDetailByProjectId(Long projectId);
}
