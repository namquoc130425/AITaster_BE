package com.example.AiTaster.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserBankAccountResponse {
    Long userBankAccountId;
    Long userId;
    String bankCode;
    String accountNumber;
    String accountHolderName;
    Boolean verified;
    Boolean isDefault;
    LocalDateTime createAt;
    LocalDateTime updateAt;
}
