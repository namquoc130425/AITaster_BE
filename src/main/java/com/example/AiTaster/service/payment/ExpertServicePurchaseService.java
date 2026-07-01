package com.example.AiTaster.service.payment;

import com.example.AiTaster.constant.PaymentReferenceType;
import com.example.AiTaster.constant.ServiceStatus;
import com.example.AiTaster.constant.TransactionType;
import com.example.AiTaster.dto.response.SepayCheckoutFormResponse;
import com.example.AiTaster.dto.response.SepayPurchasePaymentResponse;
import com.example.AiTaster.entity.*;
import com.example.AiTaster.exception.GlobalException;
import com.example.AiTaster.mapper.PaymentTransactionMapper;
import com.example.AiTaster.repository.ClientProfileRepo;
import com.example.AiTaster.repository.ExpertServiceRepo;
import com.example.AiTaster.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ExpertServicePurchaseService {
    private final ExpertServiceRepo expertServiceRepo;
    private final ClientProfileRepo clientProfileRepo;
    private final CurrentUserService currentUserService;
    private final MoneyMovementService moneyMovementService;
    private final PlatformFeeCalculator platformFeeCalculator;
    private final PendingPaymentService pendingPaymentService;
    private final SepayGateway sepayGateway;
    private final PaymentTransactionMapper paymentTransactionMapper;

    // Client dùng ví nội bộ để mua ExpertService
    // Luồng: ví client → ví expert (trừ phí sàn → ví admin)
    @Transactional
    public PaymentTransaction purchaseService(Long serviceId) {
        User user = currentUserService.getCurrentUser();

        ClientProfile clientProfile = clientProfileRepo.findByUser(user).orElseThrow(() -> new GlobalException(403, "Only client can purchase service"));
        ExpertService expertService = expertServiceRepo.findById(serviceId).orElseThrow(() -> new GlobalException(404, "Expert service not found"));

        if(!expertService.getServiceStatus().equals(ServiceStatus.OPEN)) {
            throw new GlobalException(400, "Service is not available");
        }
        Long clientUserId = clientProfile.getUser().getUserId();
        // expertService → expertProfile → user
        Long expertUserId = expertService.getExpertProfile().getUser().getUserId();


        BigDecimal amount = expertService.getServiceFee();
        BigDecimal balanceAmount = moneyMovementService.calculateFee(amount);

        // Chuyển tiền nội bộ: ví client → ví expert
        // Nếu ví client không đủ tiền → MoneyMovementService tự throw lỗi
        return moneyMovementService.moneyTransactionManagement(
                clientUserId,                            // fromId : client mua -> bị trừ tiền trong ví
                expertUserId,                            // toId : expert mua -> được cộng tiền vào ví
                TransactionType.EXPERT_SERVICE_PURCHASE, // nghiệp vụ client mua sản phẩm có sẵn
                serviceId,                               // referenceId: id của service
                PaymentReferenceType.EXPERT_SERVICE,     // mua service
                "Purchase service " + serviceId + ": " + expertService.getServiceName(),
                amount,
                balanceAmount ,
                null

        );
    }

    @Transactional
    public SepayPurchasePaymentResponse createServiceSepayPayment(Long serviceId) {
        User user = currentUserService.getCurrentUser();

        ClientProfile clientProfile = clientProfileRepo.findByUser(user)
                .orElseThrow(() -> new GlobalException(403, "Only client can purchase service"));

        ExpertService expertService = expertServiceRepo.findById(serviceId)
                .orElseThrow(() -> new GlobalException(404, "Expert service not found"));

        if (!ServiceStatus.OPEN.equals(expertService.getServiceStatus())) {
            throw new GlobalException(400, "Service is not available");
        }

        BigDecimal amount = expertService.getServiceFee();

        Long clientUserId = clientProfile.getUser().getUserId();
        Long expertUserId = expertService.getExpertProfile().getUser().getUserId();

        // Tao transaction PENDING de SePay thanh toan.
        // lúc này chưa công trừ tiền ví vì SePay chứa báo success.
        PaymentTransaction paymentTransaction = pendingPaymentService.createPendingPaymentTransaction(
                clientUserId,
                expertUserId,
                null,
                null,
                null,
                serviceId,
                TransactionType.EXPERT_SERVICE_PURCHASE,
                serviceId,
                PaymentReferenceType.EXPERT_SERVICE,
                amount,
                "SePay purchase service " + serviceId + ": " + expertService.getServiceName(),
                LocalDateTime.now().plusHours(1)
        );

        SepayCheckoutFormResponse checkoutForm = sepayGateway.createCheckoutForm(paymentTransaction);

        return paymentTransactionMapper.toSepayPurchasePaymentResponse(paymentTransaction, checkoutForm);
    }
}
