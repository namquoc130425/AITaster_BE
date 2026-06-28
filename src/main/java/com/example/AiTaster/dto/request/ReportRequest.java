package com.example.AiTaster.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class ReportRequest {

    @NotNull(message = "FIELD_REQUIRED")
    Long reportedUserId;

    @NotBlank(message = "FIELD_REQUIRED")
    String reportTitle;

    @NotBlank(message = "FIELD_REQUIRED")
    String reportReason;

    @NotBlank(message = "FIELD_REQUIRED")
    String reportDescription;

    MultipartFile evidenceFile;
}