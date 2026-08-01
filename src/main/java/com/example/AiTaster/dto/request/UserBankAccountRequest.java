package com.example.AiTaster.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserBankAccountRequest {
    @NotBlank(message = "Mã ngân hàng không được để trống")
    String bankCode;

    @NotBlank(message = "Số tài khoản không được để trống")
    String accountNumber;

    @NotBlank(message = "Tên chủ tài khoản không được để trống")
    String accountHolderName;
}
