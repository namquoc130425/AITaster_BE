package com.example.AiTaster.repository;

import com.example.AiTaster.entity.ProjectMilestone;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProjectMilestoneRepo extends JpaRepository<ProjectMilestone, Long> {
    Optional<ProjectMilestone> findByProjectId(Long projectId);

    boolean existsByProjectId(Long projectId);

}
