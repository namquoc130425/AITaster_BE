package com.example.AiTaster.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BankAccountOtpVerifyRequest {
    @NotBlank(message = "OTP is required")
    String otp;
}
