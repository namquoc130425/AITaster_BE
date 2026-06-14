package com.example.AiTaster.service;

import com.example.AiTaster.constant.ErrorCode;
import com.example.AiTaster.dto.request.ForgotPasswordRequest;
import com.example.AiTaster.dto.request.ResetPasswordRequest;
import com.example.AiTaster.entity.PasswordResetOtp;
import com.example.AiTaster.entity.User;
import com.example.AiTaster.exception.GlobalException;
import com.example.AiTaster.repository.PasswordResetOtpRepo;
import com.example.AiTaster.repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class ForgotPasswordService {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private PasswordResetOtpRepo passwordResetOtpRepo;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final int OTP_EXPIRE_MINUTES = 5;

    public void forgotPassword(ForgotPasswordRequest request) {

        User user = userRepo.findByEmail(request.getEmail())
                .orElseThrow(() -> new GlobalException(
                        ErrorCode.USER_NOT_FOUND.getCode(),
                        ErrorCode.USER_NOT_FOUND.getMessage()
                ));

        String otp = generateOtp();

        PasswordResetOtp resetOtp = PasswordResetOtp.builder()
                .email(user.getEmail())
                .otp(otp)
                .expiredAt(LocalDateTime.now().plusMinutes(OTP_EXPIRE_MINUTES))
                .used(false)
                .build();

        passwordResetOtpRepo.save(resetOtp);

        emailService.sendResetPasswordOtpEmail(
                user.getEmail(),
                otp,
                OTP_EXPIRE_MINUTES
        );
    }

    public void resetPassword(ResetPasswordRequest request) {

        PasswordResetOtp resetOtp = passwordResetOtpRepo
                .findTopByEmailAndOtpAndUsedFalseOrderByCreatedAtDesc(
                        request.getEmail(),
                        request.getOtp()
                )
                .orElseThrow(() -> new GlobalException(
                        ErrorCode.INVALID_TOKEN.getCode(),
                        "Invalid OTP"
                ));

        if (resetOtp.getExpiredAt().isBefore(LocalDateTime.now())) {
            throw new GlobalException(
                    ErrorCode.INVALID_TOKEN.getCode(),
                    "OTP expired"
            );
        }

        User user = userRepo.findByEmail(request.getEmail())
                .orElseThrow(() -> new GlobalException(
                        ErrorCode.USER_NOT_FOUND.getCode(),
                        ErrorCode.USER_NOT_FOUND.getMessage()
                ));

        user.setPasswordHash(
                passwordEncoder.encode(request.getNewPassword())
        );

        resetOtp.setUsed(true);

        userRepo.save(user);
        passwordResetOtpRepo.save(resetOtp);
    }

    private String generateOtp() {
        return String.valueOf(
                100000 + new Random().nextInt(900000)
        );
    }
}