package com.example.AiTaster.repository;

import com.example.AiTaster.constant.DisputeStatus;
import com.example.AiTaster.entity.Dispute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Collection;
import java.util.Optional;

public interface DisputeRepo extends JpaRepository<Dispute, Long>, JpaSpecificationExecutor<Dispute> {

    Optional<Dispute> findByDisputeId(Long disputeId);

    boolean existsByProject_ProjectIdAndDisputeStatusIn(
            Long projectId,
            Collection<DisputeStatus> statuses
    );
}
