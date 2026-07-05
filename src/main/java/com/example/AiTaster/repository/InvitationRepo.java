package com.example.AiTaster.repository;

import com.example.AiTaster.constant.InvitationStatus;
import com.example.AiTaster.entity.ClientProfile;
import com.example.AiTaster.entity.ExpertApplication;
import com.example.AiTaster.entity.ExpertProfile;
import com.example.AiTaster.entity.Invitation;
import com.example.AiTaster.entity.JobPost;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface InvitationRepo extends JpaRepository<Invitation, Long> {
    Optional<Invitation> findByInvitationId(Long invitationId);

    boolean existsByExpertApplication_JobpostAndInvitationStatus(
            JobPost jobPost,
            InvitationStatus invitationStats
    );

    boolean existsByExpertApplication_JobpostAndInvitationStatusAndExpiresAtAfter(
            JobPost jobPost,
            InvitationStatus invitationStatus,
            LocalDateTime expiresAt
    );

    List<Invitation> findByInvitationStatusAndExpiresAtBefore(
            InvitationStatus invitationStatus,
            LocalDateTime now
    );

    boolean existsByExpertApplicationAndInvitationStatusIn(
            ExpertApplication expertApplication,
            Collection<InvitationStatus> invitationStatus
    );

    @EntityGraph(attributePaths = {
            "expertApplication",
            "expertApplication.jobpost",
            "expertApplication.jobpost.clientProfile",
            "expertApplication.jobpost.clientProfile.user",
            "expertApplication.expertProfile",
            "expertApplication.expertProfile.user"
    })
    List<Invitation> findByExpertApplication_Jobpost_ClientProfileOrderByCreateAtDesc(ClientProfile clientProfile);

    @EntityGraph(attributePaths = {
            "expertApplication",
            "expertApplication.jobpost",
            "expertApplication.jobpost.clientProfile",
            "expertApplication.jobpost.clientProfile.user",
            "expertApplication.expertProfile",
            "expertApplication.expertProfile.user"
    })
    List<Invitation> findByExpertApplication_ExpertProfileOrderByCreateAtDesc(ExpertProfile expertProfile);

    @EntityGraph(attributePaths = {
            "expertApplication",
            "expertApplication.jobpost",
            "expertApplication.jobpost.clientProfile",
            "expertApplication.jobpost.clientProfile.user",
            "expertApplication.expertProfile",
            "expertApplication.expertProfile.user"
    })
    @Query("""
            SELECT DISTINCT i
            FROM Invitation i
            JOIN i.expertApplication a
            JOIN a.jobpost j
            JOIN j.clientProfile c
            JOIN a.expertProfile e
            JOIN e.user u
            WHERE i.invitationStatus IN :statuses
              AND NOT EXISTS (
                  SELECT p.projectId
                  FROM Project p
                  WHERE p.invitation = i
              )
              AND (
                  (
                      :clientProfileId IS NOT NULL
                      AND c.clientProfileId = :clientProfileId
                      AND (i.clientDeleted IS NULL OR i.clientDeleted = false)
                  )
                  OR (
                      :expertProfileId IS NOT NULL
                      AND e.expertProfileId = :expertProfileId
                      AND (i.expertDeleted IS NULL OR i.expertDeleted = false)
                  )
              )
              AND (
                  :search = ''
                  OR LOWER(COALESCE(i.projectTitle, '')) LIKE LOWER(CONCAT('%', :search, '%'))
                  OR LOWER(COALESCE(i.finalRequirement, '')) LIKE LOWER(CONCAT('%', :search, '%'))
                  OR LOWER(COALESCE(j.title, '')) LIKE LOWER(CONCAT('%', :search, '%'))
                  OR LOWER(COALESCE(c.companyName, '')) LIKE LOWER(CONCAT('%', :search, '%'))
                  OR LOWER(COALESCE(c.contactName, '')) LIKE LOWER(CONCAT('%', :search, '%'))
                  OR LOWER(COALESCE(u.fullName, '')) LIKE LOWER(CONCAT('%', :search, '%'))
              )
            ORDER BY i.createAt DESC
            """)
    List<Invitation> findMyProjectInvitationsWithoutProject(
            @Param("clientProfileId") Long clientProfileId,
            @Param("expertProfileId") Long expertProfileId,
            @Param("statuses") Collection<InvitationStatus> statuses,
            @Param("search") String search
    );

    @EntityGraph(attributePaths = {
            "expertApplication",
            "expertApplication.jobpost",
            "expertApplication.jobpost.clientProfile",
            "expertApplication.jobpost.clientProfile.user",
            "expertApplication.expertProfile",
            "expertApplication.expertProfile.user"
    })
    Optional<Invitation> findWithDetailByInvitationId(Long invitationId);

    @Query("""
            SELECT i
            FROM Invitation i
            WHERE i.invitationStatus = :status
              AND i.respondedAt IS NOT NULL
              AND i.respondedAt <= :deadline
              AND NOT EXISTS (
                  SELECT p.projectId
                  FROM Project p
                  WHERE p.invitation = i
              )
            """)
    List<Invitation> findAcceptedPaymentExpiredWithoutProject(
            @Param("status") InvitationStatus status,
            @Param("deadline") LocalDateTime deadline
    );
}
