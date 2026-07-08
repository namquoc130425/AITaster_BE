package com.example.AiTaster.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ResubmitExpertCertificateRequest {
    @NotBlank(message = "certificateUrl is required")
    String certificateUrl;
}
