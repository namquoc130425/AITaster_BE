package com.example.AiTaster.service.payment;

import com.example.AiTaster.constant.*;
import com.example.AiTaster.dto.request.SepayWebhookRequest;
import com.example.AiTaster.entity.ExpertService;
import com.example.AiTaster.entity.Invoices;
import com.example.AiTaster.entity.PaymentTransaction;
import com.example.AiTaster.exception.GlobalException;
import com.example.AiTaster.repository.ExpertServiceRepo;
import com.example.AiTaster.repository.PaymentTransactionRepo;
import com.example.AiTaster.service.InvoiceEmailService;
import com.example.AiTaster.service.InvoiceService;
>>>>>>> 4ceb432e65237a7ca034898d24e678aac4935384
import com.example.AiTaster.service.MoneyMovementService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class ExpertServicePurchaseWebhookHandler implements SepayPaymentHandler {
    private final ExpertServiceRepo expertServiceRepo;
    private final PaymentTransactionRepo paymentTransactionRepo;
    private final MoneyMovementService moneyMovementService;
<<<<<<< HEAD
=======
    private final InvoiceService invoiceService;
    private final ExpertServicePurchaseEventService purchaseEventService;
    private final InvoiceEmailService invoiceEmailService;


    // Kiểm tra payment này có thuộc luồng SePay mua AIService hay không.
    @Override
    public boolean supports(PaymentTransaction payment) {
        return PaymentMethod.SEPAY.equals(payment.getPaymentMethod()) && PaymentReferenceType.EXPERT_SERVICE.equals(payment.getPaymentReferenceType()) && TransactionType.EXPERT_SERVICE_PURCHASE.equals(payment.getTransactionType());
    }

    // Xử lý webhook SePay thành công, chuyển tiền, tạo invoice và gửi email invoice sau commit.
    @Override
    public void handle(PaymentTransaction payment, SepayWebhookRequest request, String providerTransactionCode, String providerContent, LocalDateTime paidAt) {
        ExpertService expertService = expertServiceRepo.findById(payment.getExpertServiceId()).orElseThrow(() -> new GlobalException(404, "Expert service not found"));

        if (!ServiceStatus.OPEN.equals(expertService.getServiceStatus())) {
            markFailed(payment, providerTransactionCode, providerContent);
            return;
        }

        if (hasSuccessfulPurchase(payment.getSenderId(), expertService.getServiceId())) {
            markFailed(payment, providerTransactionCode, providerContent);
            return;
        }

        BigDecimal amount = payment.getGrossAmount();

<<<<<<< HEAD
        // calculateFee() tự tạo transaction PLATFORM_FEE cho admin
        // và trả về số tiền net expert nhận.
=======

>>>>>>> 4ceb432e65237a7ca034898d24e678aac4935384
        BigDecimal balanceAmount = moneyMovementService.calculateFee(amount);

        Long expertUserId = expertService.getExpertProfile().getUser().getUserId();

        // Tiền SePay đến từ chuyển khoản ngân hàng nên không trừ ví client.
        // Số tiền cần trừ ví = 0.
        // Số tiền nhận = số tiền net expert nhận sau phí sàn.
        PaymentTransaction successTransaction = moneyMovementService.moneyTransactionManagement(
<<<<<<< HEAD
                null,
=======
                payment.getSenderId(),          // sepay thu tiền ben ngoài hệ thống , nên deductibleAmount = 0, và Van truyen senderId de giu lai client mua service,
>>>>>>> 4ceb432e65237a7ca034898d24e678aac4935384
                expertUserId,
                TransactionType.EXPERT_SERVICE_PURCHASE,
                expertService.getServiceId(),
                PaymentReferenceType.EXPERT_SERVICE,
                "SePay purchase service " + expertService.getServiceId() + ": " + expertService.getServiceName(),
                BigDecimal.ZERO,
                balanceAmount,
                payment.getPaymentTransactionId()
        );

<<<<<<< HEAD
=======

>>>>>>> 4ceb432e65237a7ca034898d24e678aac4935384
        successTransaction.setProviderTransactionCode(providerTransactionCode);
        successTransaction.setProviderContent(providerContent);
        successTransaction.setPaidAt(paidAt);

        paymentTransactionRepo.save(successTransaction);
        // Tạo invoice AIService và gửi email invoice sau commit.
        Invoices invoice = invoiceService.createForPaidAiService(successTransaction.getPaymentTransactionId());
        runAfterCommit(() -> invoiceEmailService.enqueueAndSendForInvoice(invoice.getInvoiceId()));
        purchaseEventService.publishAfterPaymentSuccess(
                payment.getSenderId(),
                expertUserId,
                expertService.getServiceId(),
                expertService.getServiceName(),
                successTransaction.getSourceWalletId(),
                successTransaction.getTargetWalletId(),
                balanceAmount
        );
>>>>>>> 4ceb432e65237a7ca034898d24e678aac4935384
    }


    // Đánh dấu payment thất bại khi service không còn mở để bán.
    private boolean hasSuccessfulPurchase(Long clientUserId, Long serviceId) {
        return paymentTransactionRepo
                .existsBySenderIdAndTransactionTypeAndPaymentReferenceTypeAndPaymentStatusAndReferenceId(
                        clientUserId,
                        TransactionType.EXPERT_SERVICE_PURCHASE,
                        PaymentReferenceType.EXPERT_SERVICE,
                        PaymentStatus.SUCCESS,
                        serviceId
                );
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

    // Chạy gửi email sau commit; trong unit test không mở transaction thì chạy ngay.
    private void runAfterCommit(Runnable action) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            action.run();
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                action.run();
            }
        });
    }
}

