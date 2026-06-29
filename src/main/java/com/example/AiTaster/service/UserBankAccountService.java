package com.example.AiTaster.service;

import com.example.AiTaster.dto.request.UserBankAccountRequest;
import com.example.AiTaster.dto.response.UserBankAccountResponse;
import com.example.AiTaster.entity.User;
import com.example.AiTaster.entity.UserBankAccount;
import com.example.AiTaster.exception.GlobalException;
import com.example.AiTaster.repository.UserBankAccountRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserBankAccountService {
    private final CurrentUserService currentUserService;
    private final UserBankAccountRepo userBankAccountRepo;

    public UserBankAccountResponse getMyDefaultBankAccount() {
        User user = currentUserService.getCurrentUser();
        return userBankAccountRepo.findFirstByUserAndIsDefaultTrueOrderByCreateAtDesc(user)
                .map(this::toResponse)
                .orElse(null);
    }

    @Transactional
    public UserBankAccountResponse integrate(UserBankAccountRequest request) {
        User user = currentUserService.getCurrentUser();
        validateBankAccount(request);

        UserBankAccount account = userBankAccountRepo.findFirstByUserAndIsDefaultTrueOrderByCreateAtDesc(user)
                .orElseGet(() -> UserBankAccount.builder().user(user).isDefault(true).build());

        account.setBankCode(request.getBankCode().trim().toUpperCase());
        account.setAccountNumber(request.getAccountNumber().trim());
        account.setAccountHolderName(request.getAccountHolderName().trim());
        account.setVerified(true);
        account.setIsDefault(true);

        return toResponse(userBankAccountRepo.save(account));
    }

    private void validateBankAccount(UserBankAccountRequest request) {
        if (request == null) {
            throw new GlobalException(400, "Bank account information is required");
        }
        String accountNumber = request.getAccountNumber() == null ? "" : request.getAccountNumber().trim();
        if (!accountNumber.matches("\\d{6,20}")) {
            throw new GlobalException(400, "Bank account number is invalid");
        }
        if (request.getBankCode() == null || request.getBankCode().isBlank()) {
            throw new GlobalException(400, "Bank code is required");
        }
        if (request.getAccountHolderName() == null || request.getAccountHolderName().trim().length() < 2) {
            throw new GlobalException(400, "Account holder name is invalid");
        }
    }

    private UserBankAccountResponse toResponse(UserBankAccount account) {
        return UserBankAccountResponse.builder()
                .userBankAccountId(account.getUserBankAccountId())
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
