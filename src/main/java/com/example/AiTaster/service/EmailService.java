package com.example.AiTaster.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendResetPasswordEmail(String to, String resetLink) {
        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(to);
        message.setSubject("Reset your AITasker password");
        message.setText(
                "Click this link to reset your password:\n\n" +
                        resetLink +
                        "\n\nThis link will expire in 15 minutes."
        );

        mailSender.send(message);
    }
}