package com.example.AiTaster.repository;

import com.example.AiTaster.entity.ClientProfile;
import com.example.AiTaster.entity.ExpertProposal;
import com.example.AiTaster.entity.ProposalUnlock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProposalUnlockRepo extends JpaRepository<ProposalUnlock, Long> {
    // Tìm record unlock theo proposa
    Optional<ProposalUnlock> findByProposal(ExpertProposal proposal);


    // Check proposal này đã được unlock thành công chưa.
    boolean existsByProposalAndIsUnlockedTrue(ExpertProposal proposal);


    // Tìm record unlock theo proposal và client.
    Optional<ProposalUnlock> findByProposalAndClientProfile(
            ExpertProposal proposal,
            ClientProfile clientProfile
    );

    // Check client này đã unlock proposal này chưa.
    // Nếu true thì được xem detailContent.
    boolean existsByProposalAndClientProfileAndIsUnlockedTrue(ExpertProposal proposal, ClientProfile clientProfile
    );
}
