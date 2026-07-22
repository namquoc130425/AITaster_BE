package com.example.AiTaster.repository;

import com.example.AiTaster.constant.MilestoneStep;
import com.example.AiTaster.entity.ProjectMilestone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ProjectMilestoneRepo extends JpaRepository<ProjectMilestone, Long> {
    Optional<ProjectMilestone> findByProjectId(Long projectId);

    boolean existsByProjectId(Long projectId);

    // Tìm milestone đang ở mốc 3, đã qua bước duyệt mốc 2, nhưng chưa release,
// và đã quá hạn xác nhận (deadline tính từ lúc step2 được duyệt)
    @Query("""
        SELECT m.milestoneId FROM ProjectMilestone m
        WHERE m.currentStep = :step
          AND m.finalApprovedAt IS NULL
          AND m.step2ApprovedAt IS NOT NULL
          AND m.step2ApprovedAt <= :deadline
        """)
    List<Long> findOverdueFinalConfirmationMilestoneIds(
            @Param("step") MilestoneStep step,
            @Param("deadline") LocalDateTime deadline
    );

}
