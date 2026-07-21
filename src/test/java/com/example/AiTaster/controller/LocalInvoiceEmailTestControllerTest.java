package com.example.AiTaster.controller;

import com.example.AiTaster.constant.InvoiceEmailRecipientRole;
import com.example.AiTaster.constant.InvoiceEmailType;
import com.example.AiTaster.constant.InvoiceType;
import com.example.AiTaster.entity.Invoices;
import com.example.AiTaster.service.EmailService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LocalInvoiceEmailTestControllerTest {

    @Mock
    private EmailService emailService;

    @InjectMocks
    private LocalInvoiceEmailTestController controller;

    // Kiểm tra endpoint test AIService gửi đúng invoice email tới client cố định.
    @Test
    void sendAiServiceInvoice_sendsOnlyClientEmail() {
        LocalInvoiceEmailTestController.LocalInvoiceEmailTestRequest request =
                new LocalInvoiceEmailTestController.LocalInvoiceEmailTestRequest(
                        "LOCAL-AI-001",
                        "AI Resume Builder",
                        null,
                        BigDecimal.valueOf(1_000_000)
                );

        controller.sendAiServiceInvoice(request);

        ArgumentCaptor<Invoices> invoiceCaptor = ArgumentCaptor.forClass(Invoices.class);
        verify(emailService).sendInvoiceEmail(
                eq("diennvtse184561@fpt.edu.vn"),
                eq(InvoiceEmailRecipientRole.CLIENT),
                eq(InvoiceEmailType.SERVICE_ORDER_PAID),
                invoiceCaptor.capture(),
                eq("Client Local Test"),
                eq("AI Resume Builder"),
                eq(null)
        );
        assertThat(invoiceCaptor.getValue().getInvoiceCode()).isEqualTo("LOCAL-AI-001");
        assertThat(invoiceCaptor.getValue().getInvoiceType()).isEqualTo(InvoiceType.SERVICE_ORDER);
    }

    // Kiểm tra endpoint test project gửi đúng invoice email tới cả client và expert cố định.
    @Test
    void sendProjectCompletionInvoice_sendsClientAndExpertEmails() {
        LocalInvoiceEmailTestController.LocalInvoiceEmailTestRequest request =
                new LocalInvoiceEmailTestController.LocalInvoiceEmailTestRequest(
                        "LOCAL-PROJECT-001",
                        null,
                        "AI Chatbot Project",
                        BigDecimal.valueOf(2_500_000)
                );

        controller.sendProjectCompletionInvoice(request);

        ArgumentCaptor<Invoices> invoiceCaptor = ArgumentCaptor.forClass(Invoices.class);
        verify(emailService).sendInvoiceEmail(
                eq("diennvtse184561@fpt.edu.vn"),
                eq(InvoiceEmailRecipientRole.CLIENT),
                eq(InvoiceEmailType.PROJECT_COMPLETION_PAID),
                invoiceCaptor.capture(),
                eq("Client Local Test"),
                eq(null),
                eq("AI Chatbot Project")
        );
        verify(emailService).sendInvoiceEmail(
                eq("ngodien2905@gmail.com"),
                eq(InvoiceEmailRecipientRole.EXPERT),
                eq(InvoiceEmailType.PROJECT_COMPLETION_PAID),
                any(Invoices.class),
                eq("Expert Local Test"),
                eq(null),
                eq("AI Chatbot Project")
        );
        verify(emailService, times(2)).sendInvoiceEmail(
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any()
        );
        assertThat(invoiceCaptor.getValue().getInvoiceCode()).isEqualTo("LOCAL-PROJECT-001");
        assertThat(invoiceCaptor.getValue().getInvoiceType()).isEqualTo(InvoiceType.PROJECT_COMPLETION);
    }
}
