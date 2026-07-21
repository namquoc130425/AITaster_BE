package com.example.AiTaster.service.payment;

import com.example.AiTaster.constant.PaymentReferenceType;
import com.example.AiTaster.constant.ServiceStatus;
import com.example.AiTaster.constant.TransactionType;
import com.example.AiTaster.dto.request.SepayWebhookRequest;
import com.example.AiTaster.entity.ExpertProfile;
import com.example.AiTaster.entity.ExpertService;
import com.example.AiTaster.entity.Invoices;
import com.example.AiTaster.entity.PaymentTransaction;
import com.example.AiTaster.entity.User;
import com.example.AiTaster.repository.ExpertServiceRepo;
import com.example.AiTaster.repository.PaymentTransactionRepo;
import com.example.AiTaster.service.InvoiceEmailService;
import com.example.AiTaster.service.InvoiceService;
import com.example.AiTaster.service.MoneyMovementService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExpertServicePurchaseWebhookHandlerTest {

    @Mock
    private ExpertServiceRepo expertServiceRepo;

    @Mock
    private PaymentTransactionRepo paymentTransactionRepo;

    @Mock
    private MoneyMovementService moneyMovementService;

    @Mock
    private InvoiceService invoiceService;

    @Mock
    private ExpertServicePurchaseEventService purchaseEventService;

    @Mock
    private InvoiceEmailService invoiceEmailService;

    @InjectMocks
    private ExpertServicePurchaseWebhookHandler webhookHandler;

    // Kiểm tra webhook SePay mua AIService sẽ gửi email invoice sau khi tạo invoice thành công.
    @Test
    void handle_sendsInvoiceEmailAfterSepayInvoiceCreated() {
        User expert = User.builder()
                .userId(20L)
                .build();
        ExpertProfile expertProfile = ExpertProfile.builder()
                .user(expert)
                .build();
        ExpertService expertService = ExpertService.builder()
                .serviceId(5L)
                .serviceName("Prompt Builder")
                .serviceStatus(ServiceStatus.OPEN)
                .expertProfile(expertProfile)
                .build();
        PaymentTransaction pendingPayment = PaymentTransaction.builder()
                .paymentTransactionId(90L)
                .senderId(10L)
                .expertServiceId(5L)
                .grossAmount(BigDecimal.valueOf(1_000_000))
                .build();
        PaymentTransaction successTransaction = PaymentTransaction.builder()
                .paymentTransactionId(91L)
                .sourceWalletId(1000L)
                .targetWalletId(2000L)
                .build();
        Invoices invoice = new Invoices();
        invoice.setInvoiceId(78L);

        when(expertServiceRepo.findById(5L)).thenReturn(Optional.of(expertService));
        when(moneyMovementService.calculateFee(BigDecimal.valueOf(1_000_000))).thenReturn(BigDecimal.valueOf(900_000));
        when(moneyMovementService.moneyTransactionManagement(
                any(),
                any(),
                any(TransactionType.class),
                any(),
                any(PaymentReferenceType.class),
                any(),
                any(),
                any(),
                any()
        )).thenReturn(successTransaction);
        when(invoiceService.createForPaidAiService(91L)).thenReturn(invoice);

        webhookHandler.handle(
                pendingPayment,
                new SepayWebhookRequest(),
                "SEPAY-001",
                "AITASKER PAYMENT",
                LocalDateTime.now()
        );

        verify(paymentTransactionRepo).save(successTransaction);
        verify(invoiceEmailService).enqueueAndSendForInvoice(78L);
    }
}
