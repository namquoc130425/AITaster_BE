package com.example.AiTaster.repository;

import com.example.AiTaster.entity.PasswordResetOtp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetOtpRepo extends JpaRepository<PasswordResetOtp, Long> {

    Optional<PasswordResetOtp> findTopByEmailAndOtpAndUsedFalseOrderByCreatedAtDesc(
            String email,
            String otp
    );
}