package com.example.AiTaster.repository;

import com.example.AiTaster.constant.DisputeStatus;
import com.example.AiTaster.entity.Dispute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface DisputeRepo extends JpaRepository<Dispute, Long>, JpaSpecificationExecutor<Dispute> {

    Optional<Dispute> findByDisputeId(Long disputeId);

    List<Dispute> findByReporter_UserIdOrReportedAgainst_UserIdOrderByCreateAtDesc(
            Long reporterUserId,
            Long reportedAgainstUserId
    );

    boolean existsByProject_ProjectIdAndDisputeStatusIn(
            Long projectId,
            Collection<DisputeStatus> statuses
    );
}
