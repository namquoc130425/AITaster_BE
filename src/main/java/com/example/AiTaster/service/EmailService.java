package com.example.AiTaster.service;

import com.example.AiTaster.constant.InvoiceEmailRecipientRole;
import com.example.AiTaster.constant.InvoiceEmailType;
import com.example.AiTaster.entity.Invoices;
import com.example.AiTaster.entity.User;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final ApplicationEventPublisher eventPublisher;

    // Gửi email hóa đơn bằng template phù hợp với loại invoice email.
    public void sendInvoiceEmail(
            String to,
            InvoiceEmailRecipientRole recipientRole,
            InvoiceEmailType emailType,
            Invoices invoice,
            String recipientName,
            String serviceName,
            String projectTitle
    ) {
        try {
            Context context = new Context();
            context.setVariable("invoice", invoice);
            context.setVariable("recipientRole", recipientRole);
            context.setVariable("emailType", emailType);
            context.setVariable("recipientName", safeDisplay(recipientName, "bạn"));
            context.setVariable("serviceName", safeDisplay(serviceName, "AI Service"));
            context.setVariable("projectTitle", safeDisplay(projectTitle, "Dự án"));

            String htmlContent = templateEngine.process(
                    resolveInvoiceTemplate(emailType),
                    context
            );

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    message,
                    true,
                    "UTF-8"
            );

            helper.setTo(to);
            helper.setSubject(buildInvoiceSubject(emailType, recipientRole, invoice));
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send invoice email", e);
        }
    }

    // Chọn template Thymeleaf theo nghiệp vụ email hóa đơn.
    private String resolveInvoiceTemplate(InvoiceEmailType emailType) {
        if (InvoiceEmailType.SERVICE_ORDER_PAID.equals(emailType)) {
            return "invoice-ai-service-paid";
        }

        if (InvoiceEmailType.PROJECT_COMPLETION_PAID.equals(emailType)) {
            return "invoice-project-completed";
        }

        throw new IllegalArgumentException("Unsupported invoice email type: " + emailType);
    }

    // Tạo subject email rõ nghiệp vụ và có mã hóa đơn để người nhận dễ đối chiếu.
    private String buildInvoiceSubject(
            InvoiceEmailType emailType,
            InvoiceEmailRecipientRole recipientRole,
            Invoices invoice
    ) {
        String invoiceCode = invoice != null && invoice.getInvoiceCode() != null
                ? invoice.getInvoiceCode()
                : "";

        if (InvoiceEmailType.SERVICE_ORDER_PAID.equals(emailType)) {
            return "AITasker - Hóa đơn mua AI Service " + invoiceCode;
        }

        if (InvoiceEmailType.PROJECT_COMPLETION_PAID.equals(emailType)
                && InvoiceEmailRecipientRole.EXPERT.equals(recipientRole)) {
            return "AITasker - Thông tin thanh toán dự án " + invoiceCode;
        }

        if (InvoiceEmailType.PROJECT_COMPLETION_PAID.equals(emailType)) {
            return "AITasker - Hóa đơn dự án hoàn thành " + invoiceCode;
        }

        return "AITasker - Hóa đơn " + invoiceCode;
    }

    // Trả về fallback khi text hiển thị bị null hoặc rỗng.
    private String safeDisplay(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    // Gửi email OTP cho luồng quên mật khẩu bằng template reset-password-otp.
    public void sendResetPasswordOtpEmail(
            String to,
            String otp,
            int expireMinutes
    ) {
        try {
            Context context = new Context();
            context.setVariable("otp", otp);
            context.setVariable("expireMinutes", expireMinutes);

            String htmlContent = templateEngine.process(
                    "reset-password-otp",
                    context
            );

            MimeMessage message = mailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(
                    message,
                    true,
                    "UTF-8"
            );

            helper.setTo(to);
            helper.setSubject("AITasker Reset Password OTP");
            helper.setText(htmlContent, true);

            mailSender.send(message);

        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email");
        }
    }

    // Gửi email OTP xác thực tài khoản ngân hàng bằng template bank-account-otp.
    public void sendWelcomeEmail(
            String to,
            String displayName,
            String role
    ) {
        try {
            Context context = new Context();
            context.setVariable("displayName", safeDisplay(displayName, "there"));
            context.setVariable("role", safeDisplay(role, "member"));

            String htmlContent = templateEngine.process(
                    "welcome-user",
                    context
            );

            MimeMessage message = mailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(
                    message,
                    true,
                    "UTF-8"
            );

            helper.setTo(to);
            helper.setSubject("Welcome to AITasker");
            helper.setText(htmlContent, true);

            mailSender.send(message);

        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send welcome email", e);
        }
    }

    public void sendBankAccountOtpEmail(
            String to,
            String otp,
            int expireMinutes,
            String bankCode,
            String maskedAccountNumber
    ) {
        try {
            Context context = new Context();
            context.setVariable("otp", otp);
            context.setVariable("expireMinutes", expireMinutes);
            context.setVariable("bankCode", bankCode);
            context.setVariable("maskedAccountNumber", maskedAccountNumber);

            String htmlContent = templateEngine.process(
                    "bank-account-otp",
                    context
            );

            MimeMessage message = mailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(
                    message,
                    true,
                    "UTF-8"
            );

            helper.setTo(to);
            helper.setSubject("AITasker bank account verification OTP");
            helper.setText(htmlContent, true);

            mailSender.send(message);

        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email");
        }
    }

    public void queueExpertApplied(User client, User expert, String jobTitle) {
        queueWorkflowEmail(
                client,
                "AITasker - Có chuyên gia mới ứng tuyển",
                "workflow-expert-applied",
                workflowVariables(client, expert, "jobTitle", safeDisplay(jobTitle, "Công việc của bạn"))
        );
    }

    public void queueInvitationReceived(User client, User expert, String projectTitle) {
        queueWorkflowEmail(
                expert,
                "AITasker - Bạn nhận được lời mời dự án mới",
                "workflow-invitation-received",
                workflowVariables(client, expert, "projectTitle", safeDisplay(projectTitle, "Dự án"))
        );
    }

    public void queueInvitationAccepted(User client, User expert, String projectTitle) {
        queueWorkflowEmail(
                client,
                "AITasker - Chuyên gia đã chấp nhận lời mời",
                "workflow-invitation-accepted",
                workflowVariables(client, expert, "projectTitle", safeDisplay(projectTitle, "Dự án"))
        );
    }

    public void queueProjectStarted(User client, User expert, String projectTitle) {
        queueWorkflowEmail(
                expert,
                "AITasker - Dự án đã chính thức bắt đầu",
                "workflow-project-started",
                workflowVariables(client, expert, "projectTitle", safeDisplay(projectTitle, "Dự án"))
        );
    }

    private Map<String, Object> workflowVariables(
            User client,
            User expert,
            String titleVariable,
            String title
    ) {
        return Map.of(
                "clientName", displayName(client, "khách hàng"),
                "expertName", displayName(expert, "chuyên gia"),
                titleVariable, title
        );
    }

    private void queueWorkflowEmail(
            User recipient,
            String subject,
            String templateName,
            Map<String, Object> variables
    ) {
        if (recipient == null || recipient.getEmail() == null || recipient.getEmail().isBlank()) {
            return;
        }

        eventPublisher.publishEvent(new WorkflowEmailRequestedEvent(
                recipient.getEmail(),
                subject,
                templateName,
                variables
        ));
    }

    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT,
            fallbackExecution = true
    )
    public void sendWorkflowEmailAfterCommit(WorkflowEmailRequestedEvent event) {
        try {
            sendTemplateEmail(
                    event.recipientEmail(),
                    event.subject(),
                    event.templateName(),
                    event.variables()
            );
        } catch (RuntimeException exception) {
            log.error(
                    "Failed to send workflow email with template {} to {}",
                    event.templateName(),
                    event.recipientEmail(),
                    exception
            );
        }
    }

    public void sendTemplateEmail(
            String to,
            String subject,
            String templateName,
            Map<String, Object> variables
    ) {
        try {
            Context context = new Context();
            context.setVariables(variables == null ? Map.of() : variables);

            String htmlContent = templateEngine.process(templateName, context);
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email", e);
        }
    }

    private String displayName(User user, String fallback) {
        if (user == null) {
            return fallback;
        }
        if (user.getFullName() != null && !user.getFullName().isBlank()) {
            return user.getFullName();
        }
        if (user.getUsername() != null && !user.getUsername().isBlank()) {
            return user.getUsername();
        }
        return fallback;
    }
}

record WorkflowEmailRequestedEvent(
        String recipientEmail,
        String subject,
        String templateName,
        Map<String, Object> variables
) {
}
