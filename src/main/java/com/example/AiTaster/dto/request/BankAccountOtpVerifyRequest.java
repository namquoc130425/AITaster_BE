package com.example.AiTaster.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BankAccountOtpVerifyRequest {
    @NotBlank(message = "Mã OTP không được để trống")
    String otp;
}
