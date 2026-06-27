package com.example.AiTaster.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class CreateReportRequest {

    @NotNull
    Long reportedUserId;

    @NotBlank
    String reportTitle;

    @NotBlank
    String reportReason;

    String reportDescription;

    MultipartFile evidenceFile;
}