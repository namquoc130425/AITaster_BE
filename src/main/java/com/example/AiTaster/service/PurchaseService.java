package com.example.AiTaster.service;

import com.example.AiTaster.constant.*;
import com.example.AiTaster.dto.request.PaymentTransferRequest;
import com.example.AiTaster.dto.response.ClientServiceResponse;
import com.example.AiTaster.dto.response.PurchaseResponse;
import com.example.AiTaster.entity.*;
import com.example.AiTaster.exception.GlobalException;
import com.example.AiTaster.mapper.ExpertServiceMapper;
import com.example.AiTaster.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PurchaseService {
    private final CurrentUserService currentUserService;
    private final ClientProfileRepo clientProfileRepo;
    private final ExpertProposalRepo expertProposalRepo;
    private final ProposalUnlockRepo proposalUnlockRepo;
    private final ExpertServiceRepo expertServiceRepo;
    private final ClientServiceRepo clientServiceRepo;
    private final UserWalletRepo userWalletRepo;
    private final UserRepo userRepo;
    private final PaymentTransferService paymentTransferService;
    private final PlatformFeeCalculator platformFeeCalculator;
    private final InvoiceService invoiceService;
    private final NotificationService notificationService;
    private final ExpertApplicationRepo expertApplicationRepo;
    private final ExpertServiceMapper expertServiceMapper;

    @Value("${app.platform.admin-username:admin}")
    private String adminUsername;

    @Transactional
    public PurchaseResponse purchaseProposal(Long proposalId) {
        ClientProfile clientProfile = getCurrentClientProfile();
        ExpertProposal proposal = expertProposalRepo.findExpertProposalByProposalId(proposalId)
                .orElseThrow(() -> new GlobalException(404, "Proposal not found"));

        if (Boolean.TRUE.equals(proposal.getIsDeleted())) {
            throw new GlobalException(400, "Proposal was deleted");
        }

        ExpertApplication application = proposal.getExpertApplication();
        if (!application.getJobpost().getClientProfile().getClientProfileId().equals(clientProfile.getClientProfileId())) {
            throw new GlobalException(403, "You are not owner of this job post");
        }

        if (proposalUnlockRepo.existsByProposalAndClientProfileAndIsUnlockedTrue(proposal, clientProfile)) {
            return PurchaseResponse.builder().message("Proposal already unlocked").build();
        }

        PurchaseSettlement settlement = settleWalletPurchase(
                clientProfile.getUser(),
                application.getExpertProfile().getUser(),
                proposal.getPriceToUnlock(),
                TransactionType.PROPOSAL_PURCHASE,
                PaymentReferenceType.PROPOSAL_UNLOCK,
                proposalId,
                null,
                "Purchase proposal " + proposal.getProposalId()
        );

        ProposalUnlock unlock = ProposalUnlock.builder()
                .proposal(proposal)
                .clientProfile(clientProfile)
                .paymentTransactionId(settlement.mainTransaction.getPaymentTransactionId())
                .amount(proposal.getPriceToUnlock())
                .isUnlocked(true)
                .unlockedAt(LocalDateTime.now())
                .build();
        ProposalUnlock savedUnlock = proposalUnlockRepo.save(unlock);

        Invoice invoice = invoiceService.createPaidInvoice(
                InvoiceType.PROPOSAL_PURCHASE,
                clientProfile.getUser().getUserId(),
                application.getExpertProfile().getUser().getUserId(),
                null,
                null,
                settlement.mainTransaction.getPaymentTransactionId(),
                proposal.getPriceToUnlock(),
                settlement.platformFee,
                "Proposal purchase invoice"
        );

        notificationService.notifyProposalPurchased(proposal, clientProfile);

        return PurchaseResponse.builder()
                .message("Proposal purchased successfully")
                .paymentTransactionId(settlement.mainTransaction.getPaymentTransactionId())
                .invoice(invoiceService.toResponse(invoice))
                .build();
    }

    @Transactional
    public PurchaseResponse purchaseExpertService(Long serviceId) {
        ClientProfile clientProfile = getCurrentClientProfile();
        ExpertService expertService = expertServiceRepo.findById(serviceId)
                .orElseThrow(() -> new GlobalException(404, "AI service not found"));

        if (!ServiceStatus.OPEN.equals(expertService.getServiceStatus())) {
            throw new GlobalException(400, "AI service is not available");
        }

        if (clientServiceRepo.existsByClientProfileAndExpertService(clientProfile, expertService)) {
            throw new GlobalException(400, "You already purchased this AI service");
        }

        PurchaseSettlement settlement = settleWalletPurchase(
                clientProfile.getUser(),
                expertService.getExpertProfile().getUser(),
                expertService.getServiceFee(),
                TransactionType.EXPERT_SERVICE_PURCHASE,
                PaymentReferenceType.EXPERT_SERVICE,
                expertService.getServiceId(),
                expertService.getServiceId(),
                "Purchase AI service " + expertService.getServiceId()
        );

        ServiceFile file = expertService.getServiceFile();
        ClientService clientService = ClientService.builder()
                .clientProfile(clientProfile)
                .expertService(expertService)
                .paymentTransactionId(settlement.mainTransaction.getPaymentTransactionId())
                .serviceName(expertService.getServiceName())
                .serviceType(file != null && file.getProductType() != null ? file.getProductType().name() : "AI_SERVICE")
                .description(expertService.getServiceDescription())
                .serviceFile(file != null ? file.getProductFile() : null)
                .instructionFile(file != null ? file.getFileContent() : null)
                .videoDemo(expertService.getVideoDemo())
                .version(1)
                .receivedAt(LocalDateTime.now())
                .build();

        Invoice invoice = invoiceService.createPaidInvoice(
                InvoiceType.EXPERT_SERVICE_PURCHASE,
                clientProfile.getUser().getUserId(),
                expertService.getExpertProfile().getUser().getUserId(),
                null,
                null,
                settlement.mainTransaction.getPaymentTransactionId(),
                expertService.getServiceFee(),
                settlement.platformFee,
                "AI service purchase invoice"
        );

        clientService.setInvoiceId(invoice.getInvoiceId());
        ClientService savedClientService = clientServiceRepo.save(clientService);

        notificationService.notifyExpertServicePurchased(expertService, clientProfile);

        return PurchaseResponse.builder()
                .message("AI service purchased successfully")
                .paymentTransactionId(settlement.mainTransaction.getPaymentTransactionId())
                .invoice(invoiceService.toResponse(invoice))
                .clientServiceId(savedClientService.getClientServiceId())
                .build();
    }

    public List<ClientServiceResponse> getMyPurchasedServices() {
        ClientProfile clientProfile = getCurrentClientProfile();
        return clientServiceRepo.findByClientProfileOrderByCreatedAtDesc(clientProfile)
                .stream()
                .map(this::toClientServiceResponse)
                .toList();
    }

    private PurchaseSettlement settleWalletPurchase(
            User clientUser,
            User expertUser,
            BigDecimal totalAmount,
            TransactionType transactionType,
            PaymentReferenceType referenceType,
            Long referenceId,
            Long expertServiceId,
            String description
    ) {
        if (totalAmount == null || totalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new GlobalException(400, "Amount is invalid");
        }

        User adminUser = userRepo.findByUsername(adminUsername)
                .orElseThrow(() -> new GlobalException(404, "Admin user not found"));
        UserWallet clientWallet = userWalletRepo.findByUser(clientUser)
                .orElseThrow(() -> new GlobalException(404, "Client wallet not found"));
        UserWallet expertWallet = userWalletRepo.findByUser(expertUser)
                .orElseThrow(() -> new GlobalException(404, "Expert wallet not found"));
        UserWallet adminWallet = userWalletRepo.findByUser(adminUser)
                .orElseThrow(() -> new GlobalException(404, "Admin wallet not found"));

        BigDecimal platformFee = platformFeeCalculator.calculatePlatformFee(totalAmount);
        BigDecimal expertAmount = totalAmount.subtract(platformFee);

        PaymentTransaction mainTransaction = paymentTransferService.transfer(PaymentTransferRequest.builder()
                .expertServiceId(expertServiceId)
                .senderId(clientUser.getUserId())
                .receiverId(expertUser.getUserId())
                .sourceWalletId(clientWallet.getUserWalletId())
                .targetWalletId(expertWallet.getUserWalletId())
                .fromAmount(totalAmount)
                .receiveAmount(expertAmount)
                .transactionType(transactionType)
                .paymentMethod(PaymentMethod.WALLET)
                .paymentStatus(PaymentStatus.SUCCESS)
                .referenceId(referenceId)
                .paymentReferenceType(referenceType)
                .providerName("INTERNAL")
                .description(description)
                .debitSourceWallet(true)
                .creditTargetWallet(true)
                .build());

        if (platformFee.compareTo(BigDecimal.ZERO) > 0) {
            paymentTransferService.transfer(PaymentTransferRequest.builder()
                    .expertServiceId(expertServiceId)
                    .senderId(clientUser.getUserId())
                    .receiverId(adminUser.getUserId())
                    .sourceWalletId(null)
                    .targetWalletId(adminWallet.getUserWalletId())
                    .fromAmount(BigDecimal.ZERO)
                    .receiveAmount(platformFee)
                    .transactionType(TransactionType.PLATFORM_FEE)
                    .paymentMethod(PaymentMethod.WALLET)
                    .paymentStatus(PaymentStatus.SUCCESS)
                    .referenceId(referenceId)
                    .paymentReferenceType(referenceType)
                    .providerName("INTERNAL")
                    .description("Platform fee for " + transactionType.name())
                    .creditTargetWallet(true)
                    .build());
        }

        return new PurchaseSettlement(mainTransaction, platformFee);
    }

    private ClientProfile getCurrentClientProfile() {
        User user = currentUserService.getCurrentUser();
        return clientProfileRepo.findByUser(user)
                .orElseThrow(() -> new GlobalException(403, "Only client can use this API"));
    }

    private ClientServiceResponse toClientServiceResponse(ClientService clientService) {
        return ClientServiceResponse.builder()
                .clientServiceId(clientService.getClientServiceId())
                .serviceId(clientService.getExpertService().getServiceId())
                .invoiceId(clientService.getInvoiceId())
                .paymentTransactionId(clientService.getPaymentTransactionId())
                .serviceName(clientService.getServiceName())
                .serviceType(clientService.getServiceType())
                .description(clientService.getDescription())
                .serviceFile(clientService.getServiceFile())
                .videoDemo(clientService.getVideoDemo())
                .instructionFile(clientService.getInstructionFile())
                .version(clientService.getVersion())
                .receivedAt(clientService.getReceivedAt())
                .expiredAt(clientService.getExpiredAt())
                .createdAt(clientService.getCreatedAt())
                .expertService(expertServiceMapper.toResponse(clientService.getExpertService()))
                .build();
    }

    private record PurchaseSettlement(PaymentTransaction mainTransaction, BigDecimal platformFee) {
    }
}
