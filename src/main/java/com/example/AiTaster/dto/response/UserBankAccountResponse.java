package com.example.AiTaster.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserBankAccountResponse {
    Long userBankAccountId;
    String bankCode;
    String accountNumber;
    String accountHolderName;
    Boolean verified;
    Boolean isDefault;
    LocalDateTime createAt;
    LocalDateTime updateAt;
}
