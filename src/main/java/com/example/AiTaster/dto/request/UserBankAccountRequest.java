package com.example.AiTaster.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserBankAccountRequest {
    @NotBlank(message = "Mã ngân hàng là bắt buộc")
    String bankCode;

    @NotBlank(message = "Số tài khoản là bắt buộc")
    String accountNumber;

    @NotBlank(message = "Tên chủ tài khoản là bắt buộc")
    String accountHolderName;
}
