package com.example.AiTaster.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RejectExpertVerificationRequest {
    @NotBlank(message = "Lý do từ chối không được để trống")
    String reason;
}
