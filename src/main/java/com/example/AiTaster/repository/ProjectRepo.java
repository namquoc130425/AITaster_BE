package com.example.AiTaster.repository;

import com.example.AiTaster.entity.Invitation;
import com.example.AiTaster.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProjectRepo extends JpaRepository<Project, Long> {

    boolean existsByInvitation(Invitation invitation);

    Optional<Project> findByProjectId(Long projectId);

    Optional<Project> findByInvitation(Invitation invitation);
}
