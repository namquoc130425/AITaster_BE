package com.example.AiTaster.service.payment;

import com.example.AiTaster.constant.PaymentMethod;
import com.example.AiTaster.constant.PaymentReferenceType;
import com.example.AiTaster.constant.PaymentStatus;
import com.example.AiTaster.constant.TransactionType;
import com.example.AiTaster.dto.request.SepayWebhookRequest;
import com.example.AiTaster.entity.ClientProfile;
import com.example.AiTaster.entity.ExpertProposal;
import com.example.AiTaster.entity.PaymentTransaction;
import com.example.AiTaster.entity.ProposalUnlock;
import com.example.AiTaster.exception.GlobalException;
import com.example.AiTaster.repository.ClientProfileRepo;
import com.example.AiTaster.repository.ExpertProposalRepo;
import com.example.AiTaster.repository.PaymentTransactionRepo;
import com.example.AiTaster.repository.ProposalUnlockRepo;
import com.example.AiTaster.service.MoneyMovementService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
@Component
@RequiredArgsConstructor
public class ProposalPurchaseWebhookHandler  implements SepayPaymentHandler {
    private final ExpertProposalRepo expertProposalRepo;
    private final ClientProfileRepo clientProfileRepo;
    private final ProposalUnlockRepo proposalUnlockRepo;
    private final PaymentTransactionRepo paymentTransactionRepo;
    private final MoneyMovementService moneyMovementService;

    @Override
    public boolean supports(PaymentTransaction payment) {
        return PaymentMethod.SEPAY.equals(payment.getPaymentMethod())
                && PaymentReferenceType.PROPOSAL_UNLOCK.equals(payment.getPaymentReferenceType())
                && TransactionType.PROPOSAL_PURCHASE.equals(payment.getTransactionType());
    }

    @Override
    public void handle(PaymentTransaction payment, SepayWebhookRequest request, String providerTransactionCode, String providerContent, LocalDateTime paidAt) {
        ExpertProposal proposal = expertProposalRepo.findExpertProposalByProposalId(payment.getReferenceId())
                .orElseThrow(() -> new GlobalException(404, "Proposal not found"));

        if (Boolean.TRUE.equals(proposal.getIsDeleted())) {
            markFailed(payment, providerTransactionCode, providerContent);
            return;
        }

        ClientProfile clientProfile = clientProfileRepo.findByUser_UserId(payment.getSenderId())
                .orElseThrow(() -> new GlobalException(404, "Client profile not found"));

        boolean alreadyUnlocked = proposalUnlockRepo
                .existsByProposalAndClientProfileAndIsUnlockedTrue(proposal, clientProfile);

        if (alreadyUnlocked) {
            markFailed(payment, providerTransactionCode, providerContent);
            return;
        }

        BigDecimal amount = payment.getGrossAmount();

        // Tinh phi admin rieng.
        BigDecimal balanceAmount = moneyMovementService.calculateFee(amount);

        Long expertUserId = proposal.getExpertApplication()
                .getExpertProfile()
                .getUser()
                .getUserId();

        // SePay thanh toan tu ngan hang, nen khong tru vi client.
        PaymentTransaction successTransaction = moneyMovementService.moneyTransactionManagement(
                null,
                expertUserId,
                TransactionType.PROPOSAL_PURCHASE,
                proposal.getProposalId(),
                PaymentReferenceType.PROPOSAL_UNLOCK,
                "SePay unlock proposal " + proposal.getProposalId() + " by client " + clientProfile.getClientProfileId(),
                BigDecimal.ZERO,
                balanceAmount,
                payment.getPaymentTransactionId()
        );

        successTransaction.setProviderTransactionCode(providerTransactionCode);
        successTransaction.setProviderContent(providerContent);
        successTransaction.setPaidAt(paidAt);

        paymentTransactionRepo.save(successTransaction);

        ProposalUnlock unlock = ProposalUnlock.builder()
                .proposal(proposal)
                .clientProfile(clientProfile)
                .paymentTransactionId(successTransaction.getPaymentTransactionId())
                .amount(amount)
                .isUnlocked(true)
                .unlockedAt(paidAt != null ? paidAt : LocalDateTime.now())
                .build();

        proposalUnlockRepo.save(unlock);
    }

    private void markFailed(
            PaymentTransaction payment,
            String providerTransactionCode,
            String providerContent
    ) {
        payment.setPaymentStatus(PaymentStatus.FAILED);
        payment.setProviderTransactionCode(providerTransactionCode);
        payment.setProviderContent(providerContent);
        paymentTransactionRepo.save(payment);
    }
}
