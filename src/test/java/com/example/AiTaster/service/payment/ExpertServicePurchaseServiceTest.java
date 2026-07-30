package com.example.AiTaster.service.payment;

import com.example.AiTaster.constant.PaymentReferenceType;
import com.example.AiTaster.constant.PaymentStatus;
import com.example.AiTaster.constant.ServiceStatus;
import com.example.AiTaster.constant.TransactionType;
import com.example.AiTaster.entity.ClientProfile;
import com.example.AiTaster.entity.ExpertProfile;
import com.example.AiTaster.entity.ExpertService;
import com.example.AiTaster.entity.Invoices;
import com.example.AiTaster.entity.PaymentTransaction;
import com.example.AiTaster.entity.User;
import com.example.AiTaster.mapper.PaymentTransactionMapper;
import com.example.AiTaster.repository.ClientProfileRepo;
import com.example.AiTaster.repository.ExpertServiceRepo;
import com.example.AiTaster.repository.PaymentTransactionRepo;
import com.example.AiTaster.service.CurrentUserService;
import com.example.AiTaster.service.InvoiceEmailService;
import com.example.AiTaster.service.InvoiceService;
import com.example.AiTaster.service.MoneyMovementService;
import com.example.AiTaster.service.PendingPaymentService;
import com.example.AiTaster.service.PlatformFeeCalculator;
import com.example.AiTaster.service.SepayGateway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExpertServicePurchaseServiceTest {

    @Mock
    private ExpertServiceRepo expertServiceRepo;

    @Mock
    private ClientProfileRepo clientProfileRepo;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private MoneyMovementService moneyMovementService;

    @Mock
    private PlatformFeeCalculator platformFeeCalculator;

    @Mock
    private PendingPaymentService pendingPaymentService;

    @Mock
    private SepayGateway sepayGateway;

    @Mock
    private PaymentTransactionRepo paymentTransactionRepo;

    @Mock
    private PaymentTransactionMapper paymentTransactionMapper;

    @Mock
    private InvoiceService invoiceService;

    @Mock
    private ExpertServicePurchaseEventService purchaseEventService;

    @Mock
    private InvoiceEmailService invoiceEmailService;

    @InjectMocks
    private ExpertServicePurchaseService purchaseService;

    // Kiểm tra mua AIService bằng ví sẽ gửi email invoice sau khi invoice được tạo.
    @Test
    void purchaseService_sendsInvoiceEmailAfterInvoiceCreated() {
        User client = User.builder()
                .userId(10L)
                .build();
        ClientProfile clientProfile = ClientProfile.builder()
                .user(client)
                .build();
        User expert = User.builder()
                .userId(20L)
                .build();
        ExpertProfile expertProfile = ExpertProfile.builder()
                .user(expert)
                .build();
        ExpertService expertService = ExpertService.builder()
                .serviceId(5L)
                .serviceName("Prompt Builder")
                .serviceFee(BigDecimal.valueOf(1_000_000))
                .serviceStatus(ServiceStatus.OPEN)
                .expertProfile(expertProfile)
                .build();
        PaymentTransaction paymentTransaction = PaymentTransaction.builder()
                .paymentTransactionId(99L)
                .sourceWalletId(1000L)
                .targetWalletId(2000L)
                .build();
        Invoices invoice = new Invoices();
        invoice.setInvoiceId(77L);

        when(currentUserService.getCurrentUser()).thenReturn(client);
        when(clientProfileRepo.findByUser(client)).thenReturn(Optional.of(clientProfile));
        when(expertServiceRepo.findById(5L)).thenReturn(Optional.of(expertService));
        when(paymentTransactionRepo.existsBySenderIdAndTransactionTypeAndPaymentReferenceTypeAndPaymentStatusAndReferenceId(
                any(),
                any(TransactionType.class),
                any(PaymentReferenceType.class),
                any(PaymentStatus.class),
                any()
        )).thenReturn(false);
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
        )).thenReturn(paymentTransaction);
        when(invoiceService.createForPaidAiService(99L)).thenReturn(invoice);

        purchaseService.purchaseService(5L);

        verify(invoiceEmailService).enqueueAndSendForInvoice(77L);
    }
}
