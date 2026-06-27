package com.example.AiTaster.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReportRequest {

    @NotNull
    Long reportedUserId;

    @NotBlank
    String reportTitle;

    @NotBlank
    String reportReason;

    @NotBlank
    String reportDescription;

    String evidenceFile;
}