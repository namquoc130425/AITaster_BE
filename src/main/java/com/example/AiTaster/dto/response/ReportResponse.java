package com.example.AiTaster.dto.response;

import com.example.AiTaster.constant.ReportStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReportResponse {

    Long reportId;

    Long reporterId;
    String reporterName;

    Long reportedUserId;
    String reportedUserName;

    String reportTitle;
    String reportReason;
    String reportDescription;

    String evidenceFile;

    ReportStatus reportStatus;

    String adminResponse;

    LocalDateTime createdAt;
}