package com.example.AiTaster.repository;

import com.example.AiTaster.constant.ProjectStatus;
import com.example.AiTaster.entity.Invitation;
import com.example.AiTaster.entity.Project;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
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

    @EntityGraph(attributePaths = {
            "invitation",
            "invitation.expertApplication",
            "invitation.expertApplication.jobpost",
            "invitation.expertApplication.jobpost.clientProfile",
            "invitation.expertApplication.jobpost.clientProfile.user",
            "invitation.expertApplication.expertProfile",
            "invitation.expertApplication.expertProfile.user"
    })
    @Query("""
            SELECT DISTINCT p
            FROM Project p
            JOIN p.invitation i
            JOIN i.expertApplication a
            JOIN a.jobpost j
            JOIN j.clientProfile c
            JOIN a.expertProfile e
            JOIN e.user u
            WHERE (
                (
                    :clientProfileId IS NOT NULL
                    AND c.clientProfileId = :clientProfileId
                    AND (p.clientDeleted IS NULL OR p.clientDeleted = false)
                )
                OR (
                    :expertProfileId IS NOT NULL
                    AND e.expertProfileId = :expertProfileId
                    AND (p.expertDeleted IS NULL OR p.expertDeleted = false)
                )
            )
            AND (
                :search = ''
                OR LOWER(COALESCE(p.title, '')) LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(COALESCE(p.finalRequirementSnapshot, '')) LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(COALESCE(j.title, '')) LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(COALESCE(c.companyName, '')) LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(COALESCE(c.contactName, '')) LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(COALESCE(u.fullName, '')) LIKE LOWER(CONCAT('%', :search, '%'))
            )
            ORDER BY p.createAt DESC
            """)
    List<Project> findMyProjects(
            @Param("clientProfileId") Long clientProfileId,
            @Param("expertProfileId") Long expertProfileId,
            @Param("search") String search
    );

    long countByProjectStatus(ProjectStatus projectStatus);
}
