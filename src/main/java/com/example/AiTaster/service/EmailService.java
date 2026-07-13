package com.example.AiTaster.service;

import com.example.AiTaster.constant.InvoiceEmailRecipientRole;
import com.example.AiTaster.constant.InvoiceEmailType;
import com.example.AiTaster.entity.Invoices;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

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
}
