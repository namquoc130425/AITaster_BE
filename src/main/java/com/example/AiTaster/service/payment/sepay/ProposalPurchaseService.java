package com.example.AiTaster.service.payment.sepay;

import com.example.AiTaster.constant.PaymentReferenceType;
import com.example.AiTaster.constant.TransactionType;
import com.example.AiTaster.entity.*;
import com.example.AiTaster.exception.GlobalException;
import com.example.AiTaster.repository.*;
import com.example.AiTaster.service.CurrentUserService;
import com.example.AiTaster.service.MoneyMovementService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Service
@RequiredArgsConstructor
public class ProposalPurchaseService {
    private final CurrentUserService currentUserService;
    private final ClientProfileRepo clientProfileRepo;
    private final ExpertProposalRepo expertProposalRepo;
    private final ExpertApplicationRepo expertApplicationRepo;
    private final ProposalUnlockRepo proposalUnlockRepo;
    private final MoneyMovementService moneyMovementService;


    @Transactional
    public ProposalUnlock purchaseProposalByWallet(Long proposalId) {
        ClientProfile clientProfile = getCurrentClientProfile();

        ExpertProposal expertProposal = expertProposalRepo.findExpertProposalByProposalId(proposalId)
                .orElseThrow(() -> new GlobalException("Proposal not found"));

        if (Boolean.TRUE.equals(expertProposal.getIsDeleted())) {
            throw new GlobalException(400, "Proposal was deleted");
        }

        ExpertApplication expertApplication = expertApplicationRepo
                .findByApplicationId(expertProposal.getExpertApplication().getApplicationId())
                .orElseThrow(() -> new GlobalException("Application not found"));

        checkJobPostOwner(expertApplication.getJobpost(), clientProfile);

        boolean alreadyUnlocked = proposalUnlockRepo
                .existsByProposalAndClientProfileAndIsUnlockedTrue(expertProposal, clientProfile);

        if (alreadyUnlocked) {
            throw new GlobalException(400, "Proposal already unlocked");
        }

        BigDecimal amount = expertProposal.getPriceToUnlock();
        BigDecimal balanceAmount = moneyMovementService.calculateFee(amount);

        Long clientUserId = clientProfile.getUser().getUserId();
        Long expertUserId = expertApplication.getExpertProfile().getUser().getUserId();

        PaymentTransaction paymentTransaction = moneyMovementService.moneyTransactionManagement(
                clientUserId,
                expertUserId,
                TransactionType.PROPOSAL_PURCHASE,
                proposalId,
                PaymentReferenceType.PROPOSAL_UNLOCK,
                "Unlock proposal " + proposalId + " by client " + clientProfile.getClientProfileId(),
                amount,
                balanceAmount,
                null
        );

        ProposalUnlock unlock = ProposalUnlock.builder()
                .proposal(expertProposal)
                .clientProfile(clientProfile)
                .paymentTransactionId(paymentTransaction.getPaymentTransactionId())
                .amount(amount)
                .isUnlocked(true)
                .unlockedAt(LocalDateTime.now())
                .build();

        return proposalUnlockRepo.save(unlock);
    }

    private ClientProfile getCurrentClientProfile() {
        User user = currentUserService.getCurrentUser();

        return clientProfileRepo.findByUser(user)
                .orElseThrow(() -> new GlobalException(403, "Only client can purchase proposal"));
    }

    private void checkJobPostOwner(JobPost jobPost, ClientProfile clientProfile) {
        if (!jobPost.getClientProfile().getClientProfileId().equals(clientProfile.getClientProfileId())) {
            throw new GlobalException(403, "You are not owner of this jobpost");
        }
    }
}
