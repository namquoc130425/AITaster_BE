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
    private final InvoiceService invoiceService;
    private final ExpertServicePurchaseEventService purchaseEventService;

    // Client mua expert service bằng ví nội bộ.
    // Luồng: ví client -> ví expert; phí sàn được cộng vào ví admin.
    @Transactional
    public PaymentTransaction purchaseService(Long serviceId) {
        User user = currentUserService.getCurrentUser();

        ClientProfile clientProfile = clientProfileRepo.findByUser(user).orElseThrow(() -> new GlobalException(403, "Chỉ khách hàng mới có thể mua dịch vụ"));
        ExpertService expertService = expertServiceRepo.findById(serviceId).orElseThrow(() -> new GlobalException(404, "Không tìm thấy dịch vụ chuyên gia"));

        if(!expertService.getServiceStatus().equals(ServiceStatus.OPEN)) {
            throw new GlobalException(400, "Dịch vụ chưa khả dụng");
        }
        Long clientUserId = clientProfile.getUser().getUserId();
        // expertService -> expertProfile -> user.
        Long expertUserId = expertService.getExpertProfile().getUser().getUserId();


        BigDecimal amount = expertService.getServiceFee();
        BigDecimal balanceAmount = moneyMovementService.calculateFee(amount);
        // Chuyển tiền nội bộ: ví client -> ví expert.
        // MoneyMovementService tự throw lỗi nếu ví client không đủ tiền
        PaymentTransaction paymentTransaction = moneyMovementService.moneyTransactionManagement(
                clientUserId,
                expertUserId,
                TransactionType.EXPERT_SERVICE_PURCHASE,
                serviceId,
                PaymentReferenceType.EXPERT_SERVICE,
                "Purchase service " + serviceId + ": " + expertService.getServiceName(),
                amount,
                balanceAmount ,
                null

        );
         // tạo hóa đơn khi transaction thành công
          invoiceService.createForPaidAiService(paymentTransaction.getPaymentTransactionId());
        purchaseEventService.publishAfterPaymentSuccess(
                clientUserId,
                expertUserId,
                expertService.getServiceId(),
                expertService.getServiceName(),
                paymentTransaction.getSourceWalletId(),
                paymentTransaction.getTargetWalletId(),
                balanceAmount
        );

        return paymentTransaction;
    }

    @Transactional
    public SepayPurchasePaymentResponse createServiceSepayPayment(Long serviceId) {
        User user = currentUserService.getCurrentUser();

        ClientProfile clientProfile = clientProfileRepo.findByUser(user)
                .orElseThrow(() -> new GlobalException(403, "Chỉ khách hàng mới có thể mua dịch vụ"));

        ExpertService expertService = expertServiceRepo.findById(serviceId)
                .orElseThrow(() -> new GlobalException(404, "Không tìm thấy dịch vụ chuyên gia"));

        if (!ServiceStatus.OPEN.equals(expertService.getServiceStatus())) {
            throw new GlobalException(400, "Dịch vụ chưa khả dụng");
        }

        BigDecimal amount = expertService.getServiceFee();

        Long clientUserId = clientProfile.getUser().getUserId();
        Long expertUserId = expertService.getExpertProfile().getUser().getUserId();

        // Tạo pending SePay transaction. Chưa đổi số dư ví cho đến khi webhook success.
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
