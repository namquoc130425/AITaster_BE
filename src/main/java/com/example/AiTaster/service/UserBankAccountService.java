package com.example.AiTaster.service;

import com.example.AiTaster.dto.request.BankAccountOtpVerifyRequest;
import com.example.AiTaster.dto.request.UserBankAccountRequest;
import com.example.AiTaster.dto.response.UserBankAccountResponse;
import com.example.AiTaster.entity.User;
import com.example.AiTaster.entity.UserBankAccount;
import com.example.AiTaster.exception.GlobalException;
import com.example.AiTaster.repository.UserBankAccountRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserBankAccountService {
    private static final int OTP_EXPIRE_MINUTES = 5;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final CurrentUserService currentUserService;
    private final UserBankAccountRepo userBankAccountRepo;
    private final EmailService emailService;

    @Transactional(readOnly = true)
    public UserBankAccountResponse getMyBankAccount() {
        User user = currentUserService.getCurrentUser();

        return userBankAccountRepo.findByUser(user)
                .map(this::toResponse)
                .orElse(null);
    }

    @Transactional
    public UserBankAccountResponse requestBankAccountOtp(UserBankAccountRequest request) {
        User user = currentUserService.getCurrentUser();
        validateRequest(request);

        String otp = generateOtp();
        UserBankAccount account = userBankAccountRepo.findByUser(user)
                .orElseGet(() -> UserBankAccount.builder()
                        .user(user)
                        .build());

        account.setBankCode(request.getBankCode().trim().toUpperCase());
        account.setAccountNumber(request.getAccountNumber().trim());
        account.setAccountHolderName(request.getAccountHolderName().trim().toUpperCase());
        account.setVerified(false);
        account.setIsDefault(true);
        account.setOtpCode(otp);
        account.setOtpExpiredAt(LocalDateTime.now().plusMinutes(OTP_EXPIRE_MINUTES));

        UserBankAccount savedAccount = userBankAccountRepo.save(account);

        emailService.sendBankAccountOtpEmail(
                user.getEmail(),
                otp,
                OTP_EXPIRE_MINUTES,
                savedAccount.getBankCode(),
                maskAccountNumber(savedAccount.getAccountNumber())
        );

        return toResponse(savedAccount);
    }

    @Transactional
    public UserBankAccountResponse verifyBankAccountOtp(BankAccountOtpVerifyRequest request) {
        User user = currentUserService.getCurrentUser();
        String otp = request.getOtp() == null ? "" : request.getOtp().trim();

        UserBankAccount account = userBankAccountRepo.findByUser(user)
                .orElseThrow(() -> new GlobalException(404, "Bank account request not found"));

        if (account.getOtpCode() == null || account.getOtpExpiredAt() == null) {
            throw new GlobalException(400, "Bank account OTP was not requested");
        }

        if (account.getOtpExpiredAt().isBefore(LocalDateTime.now())) {
            throw new GlobalException(400, "Bank account OTP expired");
        }

        if (!account.getOtpCode().equals(otp)) {
            throw new GlobalException(400, "Invalid bank account OTP");
        }

        account.setVerified(true);
        account.setOtpCode(null);
        account.setOtpExpiredAt(null);

        return toResponse(userBankAccountRepo.save(account));
    }

    public UserBankAccount getVerifiedBankAccountByUserId(Long userId) {
        UserBankAccount account = userBankAccountRepo.findByUser_UserId(userId)
                .orElseThrow(() -> new GlobalException(400, "User has no payout bank account"));

        if (!Boolean.TRUE.equals(account.getVerified())) {
            throw new GlobalException(400, "Payout bank account is not verified");
        }

        return account;
    }

    @Transactional(readOnly = true)
    public UserBankAccountResponse getVerifiedBankAccountResponseByUserId(Long userId) {
        return toResponse(getVerifiedBankAccountByUserId(userId));
    }

    private void validateRequest(UserBankAccountRequest request) {
        if (request == null) {
            throw new GlobalException(400, "Bank account request is required");
        }

        if (isBlank(request.getBankCode())
                || isBlank(request.getAccountNumber())
                || isBlank(request.getAccountHolderName())) {
            throw new GlobalException(400, "Bank account information is required");
        }
    }

    private String generateOtp() {
        return String.valueOf(100000 + RANDOM.nextInt(900000));
    }

    private String maskAccountNumber(String value) {
        if (value == null || value.length() <= 4) {
            return "****";
        }

        return "*".repeat(Math.max(value.length() - 4, 4)) + value.substring(value.length() - 4);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private UserBankAccountResponse toResponse(UserBankAccount account) {
        return UserBankAccountResponse.builder()
                .userBankAccountId(account.getUserBankAccountId())
                .userId(account.getUser().getUserId())
                .bankCode(account.getBankCode())
                .accountNumber(account.getAccountNumber())
                .accountHolderName(account.getAccountHolderName())
                .verified(account.getVerified())
                .isDefault(account.getIsDefault())
                .createAt(account.getCreateAt())
                .updateAt(account.getUpdateAt())
                .build();
    }
}
