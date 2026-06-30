package com.example.AiTaster.repository;

import com.example.AiTaster.entity.ProjectEscrow;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProjectEscrowRepo extends JpaRepository<ProjectEscrow, Long> {
    Optional<ProjectEscrow> findByProjectEscrowId(long projectEscrowId);

    Optional<ProjectEscrow> findByProjectId(Long projectId);

    boolean existsByProjectId(Long projectId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT e
        FROM ProjectEscrow e
        WHERE e.projectId = :projectId
    """)
    Optional<ProjectEscrow> findByProjectIdForUpdate(@Param("projectId") Long projectId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
    SELECT e
    FROM ProjectEscrow e
    WHERE e.projectEscrowId = :projectEscrowId
""")
    Optional<ProjectEscrow> findByProjectEscrowIdForUpdate(@Param("projectEscrowId") Long projectEscrowId);
}
