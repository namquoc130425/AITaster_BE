package com.example.AiTaster.repository;

import com.example.AiTaster.constant.MilestoneStep;
import com.example.AiTaster.entity.Deliverable;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface DeliverableRepo extends JpaRepository<Deliverable, Long> {


    // lấy ra tổng version mới nhất , null thì lấy 0 từ id project và step
    @Query("SELECT coalesce(max(d.version), 0) from Deliverable d " +
            "WHERE d.projectId = :projectId and d.step = :step"
    ) int findMaxVersionByProjectIdAndStep(
            @Param("projectId")     Long projectId,@Param("step") MilestoneStep step);

    Optional<Deliverable> findTopByProjectIdAndStepOrderByVersionDesc(Long projectId, MilestoneStep step);

    List<Deliverable> findByProjectIdOrderBySubmittedAtDesc(Long projectId);



}
