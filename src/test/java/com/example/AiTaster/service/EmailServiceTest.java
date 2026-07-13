package com.example.AiTaster.service;

import com.example.AiTaster.constant.InvoiceEmailRecipientRole;
import com.example.AiTaster.constant.InvoiceEmailType;
import com.example.AiTaster.entity.Invoices;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private TemplateEngine templateEngine;

    @InjectMocks
    private EmailService emailService;

    @Test
    void sendInvoiceEmail_usesAiServiceTemplateAndSubject() throws Exception {
        MimeMessage mimeMessage = new MimeMessage(Session.getInstance(new Properties()));
        Invoices invoice = invoice("INV-AI-001");

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq("invoice-ai-service-paid"), any(Context.class)))
                .thenReturn("<p>AI Service invoice</p>");

        emailService.sendInvoiceEmail(
                "client@example.com",
                InvoiceEmailRecipientRole.CLIENT,
                InvoiceEmailType.SERVICE_ORDER_PAID,
                invoice,
                "Client",
                "Prompt Builder",
                null
        );

        verify(templateEngine).process(eq("invoice-ai-service-paid"), any(Context.class));
        verify(mailSender).send(mimeMessage);
        assertThat(mimeMessage.getSubject()).isEqualTo("AITasker - Hóa đơn mua AI Service INV-AI-001");
        assertThat(mimeMessage.getAllRecipients()[0].toString()).isEqualTo("client@example.com");
    }

    @Test
    void sendInvoiceEmail_usesProjectTemplateAndExpertSubject() throws Exception {
        MimeMessage mimeMessage = new MimeMessage(Session.getInstance(new Properties()));
        Invoices invoice = invoice("INV-PROJECT-001");

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq("invoice-project-completed"), any(Context.class)))
                .thenReturn("<p>Project invoice</p>");

        emailService.sendInvoiceEmail(
                "expert@example.com",
                InvoiceEmailRecipientRole.EXPERT,
                InvoiceEmailType.PROJECT_COMPLETION_PAID,
                invoice,
                "Expert",
                null,
                "Chatbot Project"
        );

        ArgumentCaptor<Context> contextCaptor = ArgumentCaptor.forClass(Context.class);
        verify(templateEngine).process(eq("invoice-project-completed"), contextCaptor.capture());
        verify(mailSender).send(mimeMessage);

        assertThat(mimeMessage.getSubject()).isEqualTo("AITasker - Thông tin thanh toán dự án INV-PROJECT-001");
        assertThat(contextCaptor.getValue().getVariable("projectTitle")).isEqualTo("Chatbot Project");
        assertThat(contextCaptor.getValue().getVariable("recipientRole")).isEqualTo(InvoiceEmailRecipientRole.EXPERT);
    }

    private Invoices invoice(String invoiceCode) {
        Invoices invoice = new Invoices();
        invoice.setInvoiceCode(invoiceCode);
        invoice.setCurrency("VND");
        return invoice;
    }
}
