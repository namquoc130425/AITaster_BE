package com.example.AiTaster.dto.request;

import com.example.AiTaster.constant.ReportStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReportStatusRequest {

    @NotNull(message = "FIELD_REQUIRED")
    private ReportStatus reportStatus;

    private String adminResponse;
}
