package com.example.AiTaster.repository;

import com.example.AiTaster.entity.ProjectEscrow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface ProjectEscrowRepo extends JpaRepository<ProjectEscrow, Long> {
    Optional<ProjectEscrow> findByProjectEscrowId(long projectEscrowId);

    Optional<ProjectEscrow> findByProjectId(Long projectId);

    boolean existsByProjectId(Long projectId);
}
