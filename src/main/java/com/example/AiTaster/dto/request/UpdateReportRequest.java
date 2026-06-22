package com.example.AiTaster.dto.request;

import com.example.AiTaster.constant.ReportStatus;
import lombok.Data;

@Data
public class UpdateReportRequest {

    ReportStatus reportStatus;

    String adminResponse;
}