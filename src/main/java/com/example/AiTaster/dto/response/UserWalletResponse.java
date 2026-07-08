package com.example.AiTaster.dto.response;

import com.example.AiTaster.constant.UserWalletStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class UserWalletResponse {

    Long userWalletId;

    Long userId;

    BigDecimal balance;

    BigDecimal frozenBalance;

    String currency;

    UserWalletStatus status;

    Boolean requestWithdrawal;

    BigDecimal amountRequestWithdrawal;

    String username;

    String fullName;

    String email;

    UserBankAccountResponse bankAccount;

    LocalDateTime createAt;

    LocalDateTime updateAt;
}
