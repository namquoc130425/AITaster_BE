package com.example.AiTaster.repository;

import com.example.AiTaster.constant.InvitationStatus;
import com.example.AiTaster.constant.JobpostStatus;
import com.example.AiTaster.entity.ClientProfile;
import com.example.AiTaster.entity.JobPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface JobPostRepo extends JpaRepository<JobPost, Long>, JpaSpecificationExecutor<JobPost> {
   Optional<JobPost>  findJobPostByjobPostId(Long jobPostId);
   List<JobPost> findByClientProfileOrderByCreateAtDesc(ClientProfile clientProfile); // L?y job c?a client, m?i nh?t tru?c

   List<JobPost> findByJobPostStatus(JobpostStatus jobPostStatus); // L?y t?t c? job theo status Open

   @Query("""
           SELECT DISTINCT j
           FROM JobPost j
           WHERE j.jobPostStatus = :openStatus
             AND NOT EXISTS (
                 SELECT i.invitationId
                 FROM Invitation i
                 WHERE i.expertApplication.jobpost = j
                   AND i.invitationStatus IN :reservedStatuses
             )
           ORDER BY j.createAt DESC
           """)
   List<JobPost> findPublicOpenJobPosts(
           @Param("openStatus") JobpostStatus openStatus,
           @Param("reservedStatuses") Collection<InvitationStatus> reservedStatuses
   );

   @Modifying(flushAutomatically = true)
   @Query("""
           UPDATE JobPost j
           SET j.jobPostStatus = :status
           WHERE j.jobPostId = :jobPostId
           """)
   int updateJobPostStatus(
           @Param("jobPostId") Long jobPostId,
           @Param("status") JobpostStatus status
   );

   @Modifying(flushAutomatically = true)
   @Query("""
           UPDATE JobPost j
           SET j.jobPostStatus = :closedStatus
           WHERE j.clientProfile = :clientProfile
             AND j.jobPostStatus = :openStatus
             AND j.jobPostId IN (
                 SELECT a.jobpost.jobPostId
                 FROM Invitation i
                 JOIN i.expertApplication a
                 WHERE i.invitationStatus = :acceptedStatus
             )
           """)
   int closeOpenJobPostsWithAcceptedInvitation(
           @Param("clientProfile") ClientProfile clientProfile,
           @Param("openStatus") JobpostStatus openStatus,
           @Param("closedStatus") JobpostStatus closedStatus,
           @Param("acceptedStatus") InvitationStatus acceptedStatus
   );

   @Query("""
           SELECT COUNT(j) > 0
           FROM JobPost j
           WHERE j.clientProfile = :clientProfile
             AND j.jobPostStatus IN :statuses
             AND LOWER(TRIM(j.title)) = :title
             AND LOWER(TRIM(j.requirementDescription)) = :requirementDescription
             AND LOWER(TRIM(j.businessGoal)) = :businessGoal
             AND LOWER(TRIM(j.mainFeatures)) = :mainFeatures
             AND j.budgets = :budgets
             AND LOWER(TRIM(j.timeLine)) = :timeLine
           """)
   boolean existsDuplicateActiveJobPost(
           @Param("clientProfile") ClientProfile clientProfile,
           @Param("statuses") Collection<JobpostStatus> statuses,
           @Param("title") String title,
           @Param("requirementDescription") String requirementDescription,
           @Param("businessGoal") String businessGoal,
           @Param("mainFeatures") String mainFeatures,
           @Param("budgets") BigDecimal budgets,
           @Param("timeLine") String timeLine
   );
}
