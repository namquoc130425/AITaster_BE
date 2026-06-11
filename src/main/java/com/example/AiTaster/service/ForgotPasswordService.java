package com.example.AiTaster.service;

import com.example.AiTaster.constant.ErrorCode;
import com.example.AiTaster.dto.request.ForgotPasswordRequest;
import com.example.AiTaster.dto.request.ResetPasswordRequest;
import com.example.AiTaster.entity.PasswordResetToken;
import com.example.AiTaster.entity.User;
import com.example.AiTaster.exception.GlobalException;
import com.example.AiTaster.repository.PasswordResetTokenRepo;
import com.example.AiTaster.repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class ForgotPasswordService {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private PasswordResetTokenRepo passwordResetTokenRepo;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public void forgotPassword(ForgotPasswordRequest request) {
        User user = userRepo.findByEmail(request.getEmail())
                .orElseThrow(() -> new GlobalException(
                        ErrorCode.USER_NOT_FOUND.getCode(),
                        ErrorCode.USER_NOT_FOUND.getMessage()
                ));

        String token = UUID.randomUUID().toString();

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .user(user)
                .expiredAt(LocalDateTime.now().plusMinutes(15))
                .used(false)
                .build();

        passwordResetTokenRepo.save(resetToken);

        String resetLink = "http://localhost:5173/reset-password?token=" + token;

        emailService.sendResetPasswordEmail(user.getEmail(), resetLink);
    }

    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken resetToken = passwordResetTokenRepo.findByToken(request.getToken());

        if (resetToken == null) {
            throw new GlobalException(
                    ErrorCode.INVALID_TOKEN.getCode(),
                    ErrorCode.INVALID_TOKEN.getMessage()
            );
        }

        if (resetToken.isUsed()) {
            throw new GlobalException(
                    ErrorCode.INVALID_TOKEN.getCode(),
                    "Token already used"
            );
        }

        if (resetToken.getExpiredAt().isBefore(LocalDateTime.now())) {
            throw new GlobalException(
                    ErrorCode.INVALID_TOKEN.getCode(),
                    "Token expired"
            );
        }

        User user = resetToken.getUser();

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        resetToken.setUsed(true);

        userRepo.save(user);
        passwordResetTokenRepo.save(resetToken);
    }
}