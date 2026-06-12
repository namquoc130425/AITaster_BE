package com.example.AiTaster.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResetPasswordRequest {

    @NotBlank(message = "FIELD_REQUIRED")
    String email;

    @NotBlank(message = "FIELD_REQUIRED")
    String otp;
    @NotBlank(message = "FIELD_REQUIRED")
    @Size(min = 8, message = "INVALID_SIZE")
    String newPassword;
}