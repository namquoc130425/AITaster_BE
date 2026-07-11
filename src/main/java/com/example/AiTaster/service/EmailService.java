package com.example.AiTaster.service;

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
            throw new RuntimeException("Gửi email thất bại");
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
            throw new RuntimeException("Gửi email thất bại");
        }
    }
}
