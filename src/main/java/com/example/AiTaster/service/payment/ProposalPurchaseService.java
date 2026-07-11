package com.example.AiTaster.service.payment;

import com.example.AiTaster.constant.PaymentReferenceType;
import com.example.AiTaster.constant.TransactionType;
import com.example.AiTaster.dto.response.SepayCheckoutFormResponse;
import com.example.AiTaster.dto.response.SepayPurchasePaymentResponse;
import com.example.AiTaster.entity.*;
import com.example.AiTaster.exception.GlobalException;
import com.example.AiTaster.mapper.PaymentTransactionMapper;
import com.example.AiTaster.repository.*;
import com.example.AiTaster.service.CurrentUserService;
import com.example.AiTaster.service.MoneyMovementService;
import com.example.AiTaster.service.PendingPaymentService;
import com.example.AiTaster.service.SepayGateway;
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
    private final PendingPaymentService pendingPaymentService;
    private final SepayGateway sepayGateway;
    private final PaymentTransactionMapper paymentTransactionMapper;


    @Transactional
    public ProposalUnlock purchaseProposalByWallet(Long proposalId) {
        ClientProfile clientProfile = getCurrentClientProfile();

        ExpertProposal expertProposal = expertProposalRepo.findExpertProposalByProposalId(proposalId)
                .orElseThrow(() -> new GlobalException("Không tìm thấy đề xuất"));

        if (Boolean.TRUE.equals(expertProposal.getIsDeleted())) {
            throw new GlobalException(400, "Đề xuất đã bị xóa");
        }

        ExpertApplication expertApplication = expertApplicationRepo
                .findByApplicationId(expertProposal.getExpertApplication().getApplicationId())
                .orElseThrow(() -> new GlobalException("Không tìm thấy hồ sơ ứng tuyển"));

        checkJobPostOwner(expertApplication.getJobpost(), clientProfile);

        boolean alreadyUnlocked = proposalUnlockRepo
                .existsByProposalAndClientProfileAndIsUnlockedTrue(expertProposal, clientProfile);

        if (alreadyUnlocked) {
            throw new GlobalException(400, "Đề xuất đã được mở khóa");
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



    @Transactional
    public SepayPurchasePaymentResponse createProposalSepayPayment(Long proposalId) {
        ClientProfile clientProfile = getCurrentClientProfile();

        ExpertProposal expertProposal = expertProposalRepo.findExpertProposalByProposalId(proposalId)
                .orElseThrow(() -> new GlobalException(404, "Không tìm thấy đề xuất"));

        if (Boolean.TRUE.equals(expertProposal.getIsDeleted())) {
            throw new GlobalException(400, "Đề xuất đã bị xóa");
        }

        ExpertApplication expertApplication = expertApplicationRepo
                .findByApplicationId(expertProposal.getExpertApplication().getApplicationId())
                .orElseThrow(() -> new GlobalException(404, "Không tìm thấy hồ sơ ứng tuyển"));

        checkJobPostOwner(expertApplication.getJobpost(), clientProfile);

        boolean alreadyUnlocked = proposalUnlockRepo
                .existsByProposalAndClientProfileAndIsUnlockedTrue(expertProposal, clientProfile);

        if (alreadyUnlocked) {
            throw new GlobalException(400, "Đề xuất đã được mở khóa");
        }

        BigDecimal amount = expertProposal.getPriceToUnlock();

        Long clientUserId = clientProfile.getUser().getUserId();
        Long expertUserId = expertApplication.getExpertProfile().getUser().getUserId();

        // Tạo pending SePay transaction. Proposal chỉ unlock sau khi webhook success.
        PaymentTransaction paymentTransaction = pendingPaymentService.createPendingPaymentTransaction(
                clientUserId,
                expertUserId,
                null,
                null,
                null,
                null,
                TransactionType.PROPOSAL_PURCHASE,
                proposalId,
                PaymentReferenceType.PROPOSAL_UNLOCK,
                amount,
                "SePay unlock proposal " + proposalId + " by client " + clientProfile.getClientProfileId(),
                LocalDateTime.now().plusHours(1)
        );

        SepayCheckoutFormResponse checkoutForm = sepayGateway.createCheckoutForm(paymentTransaction);

        return paymentTransactionMapper.toSepayPurchasePaymentResponse(paymentTransaction, checkoutForm);
    }

    private ClientProfile getCurrentClientProfile() {
        User user = currentUserService.getCurrentUser();

        return clientProfileRepo.findByUser(user)
                .orElseThrow(() -> new GlobalException(403, "Chỉ khách hàng mới có thể mua đề xuất"));
    }

    private void checkJobPostOwner(JobPost jobPost, ClientProfile clientProfile) {
        if (!jobPost.getClientProfile().getClientProfileId().equals(clientProfile.getClientProfileId())) {
            throw new GlobalException(403, "Bạn không phải chủ sở hữu tin tuyển dụng này");
        }
    }
}
