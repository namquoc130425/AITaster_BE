package com.example.AiTaster.service.imp;

import com.example.AiTaster.constant.ReportStatus;
import com.example.AiTaster.dto.request.ReportRequest;
import com.example.AiTaster.dto.response.ReportResponse;

import java.util.List;

public interface IReportService {

    ReportResponse createReport(ReportRequest request);

    ReportResponse updateReport(Long reportId, ReportRequest request);

    ReportResponse getReportById(Long reportId);

    List<ReportResponse> getAllReports();

    List<ReportResponse> getMyReports();

    ReportResponse changeReportStatus(
            Long reportId,
            ReportStatus reportStatus,
            String adminResponse
    );

    Void deleteReport(Long reportId);
}