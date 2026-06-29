package com.example.AiTaster.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserBankAccountRequest {
    @NotBlank(message = "Bank code is required")
    String bankCode;

    @NotBlank(message = "Account number is required")
    String accountNumber;

    @NotBlank(message = "Account holder name is required")
    String accountHolderName;
}
