package com.example.AiTaster.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ResubmitExpertCertificateRequest {
    @NotBlank(message = "Đường dẫn chứng chỉ là bắt buộc")
    String certificateUrl;
}
