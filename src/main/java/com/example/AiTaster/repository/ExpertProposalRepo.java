package com.example.AiTaster.repository;

import com.example.AiTaster.entity.ExpertApplication;
import com.example.AiTaster.entity.ExpertProfile;
import com.example.AiTaster.entity.ExpertProposal;
import com.example.AiTaster.entity.JobPost;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ExpertProposalRepo extends JpaRepository<ExpertProposal, Long> {
    Optional<ExpertProposal> findExpertProposalByProposalId(Long proposalId);

    Optional<ExpertProposal> findByExpertApplicationAndIsDeletedFalse(
            ExpertApplication expertApplication
    );

    boolean existsByExpertApplicationAndIsDeletedFalse(
            ExpertApplication expertApplication
    );

;
}
