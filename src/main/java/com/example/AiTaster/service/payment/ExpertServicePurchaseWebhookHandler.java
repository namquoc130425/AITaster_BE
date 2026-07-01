package com.example.AiTaster.service.payment;

import com.example.AiTaster.constant.*;
import com.example.AiTaster.dto.request.SepayWebhookRequest;
import com.example.AiTaster.entity.ExpertService;
import com.example.AiTaster.entity.PaymentTransaction;
import com.example.AiTaster.exception.GlobalException;
import com.example.AiTaster.repository.ExpertServiceRepo;
import com.example.AiTaster.repository.PaymentTransactionRepo;
import com.example.AiTaster.service.MoneyMovementService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class ExpertServicePurchaseWebhookHandler implements SepayPaymentHandler {
    private final ExpertServiceRepo expertServiceRepo;
    private final PaymentTransactionRepo paymentTransactionRepo;
    private final MoneyMovementService moneyMovementService;


    @Override
    public boolean supports(PaymentTransaction payment) {
        return PaymentMethod.SEPAY.equals(payment.getPaymentMethod()) && PaymentReferenceType.EXPERT_SERVICE.equals(payment.getPaymentReferenceType()) && TransactionType.EXPERT_SERVICE_PURCHASE.equals(payment.getTransactionType());
    }

    @Override
    public void handle(PaymentTransaction payment, SepayWebhookRequest request, String providerTransactionCode, String providerContent, LocalDateTime paidAt) {
        ExpertService expertService = expertServiceRepo.findById(payment.getExpertServiceId()).orElseThrow(() -> new GlobalException(404, "Expert service not found"));

        if (!ServiceStatus.OPEN.equals(expertService.getServiceStatus())) {
            markFailed(payment, providerTransactionCode, providerContent);
            return;
        }

        BigDecimal amount = payment.getGrossAmount();

        // calculateFee() tự tạo transaction PLATFORM_FEE cho admin
        // và trả về số tiền net expert nhận.
        BigDecimal balanceAmount = moneyMovementService.calculateFee(amount);

        Long expertUserId = expertService.getExpertProfile().getUser().getUserId();

        // Tiền SePay đến từ chuyển khoản ngân hàng nên không trừ ví client.
        // Số tiền cần trừ ví = 0.
        // Số tiền nhận = số tiền net expert nhận sau phí sàn.
        PaymentTransaction successTransaction = moneyMovementService.moneyTransactionManagement(
                null,
                expertUserId,
                TransactionType.EXPERT_SERVICE_PURCHASE,
                expertService.getServiceId(),
                PaymentReferenceType.EXPERT_SERVICE,
                "SePay purchase service " + expertService.getServiceId() + ": " + expertService.getServiceName(),
                BigDecimal.ZERO,
                balanceAmount,
                payment.getPaymentTransactionId()
        );

        successTransaction.setProviderTransactionCode(providerTransactionCode);
        successTransaction.setProviderContent(providerContent);
        successTransaction.setPaidAt(paidAt);

        paymentTransactionRepo.save(successTransaction);
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

