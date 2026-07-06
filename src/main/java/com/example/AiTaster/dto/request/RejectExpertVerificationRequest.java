package com.example.AiTaster.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RejectExpertVerificationRequest {
    @NotBlank(message = "Reject reason is required")
    String reason;
}
