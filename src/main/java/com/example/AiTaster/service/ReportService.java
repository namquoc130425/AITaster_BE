package com.example.AiTaster.service;

import com.example.AiTaster.constant.ReportStatus;
import com.example.AiTaster.dto.request.ReportRequest;
import com.example.AiTaster.dto.request.ReportStatusRequest;
import com.example.AiTaster.dto.response.ReportResponse;
import com.example.AiTaster.entity.Report;
import com.example.AiTaster.entity.User;
import com.example.AiTaster.exception.GlobalException;
import com.example.AiTaster.mapper.ReportMapper;
import com.example.AiTaster.repository.ReportRepo;
import com.example.AiTaster.repository.UserRepo;
import com.example.AiTaster.service.imp.IReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService implements IReportService {

    private final ReportRepo reportRepo;
    private final UserRepo userRepo;
    private final CurrentUserService currentUserService;
    private final ReportMapper reportMapper;

    @Override
    public ReportResponse createReport( // trả thêm dữ liệu profile của 2 thằng báo và bị báo
            ReportRequest request
    ) {

        User reporter =
                currentUserService.getCurrentUser();

        User reportedUser =
                userRepo.findById(
                        request.getReportedUserId()
                ).orElseThrow(
                        () -> new GlobalException(
                                400,
                                "User not found"
                        )
                );

        Report report =
                reportMapper.toEntity(
                        request
                );

        report.setReporter(reporter);
        report.setReportedUser(reportedUser);
        report.setReportStatus(
                ReportStatus.PENDING
        );

        report =
                reportRepo.save(report);

        return reportMapper.toResponse(
                report
        );
    }

    @Override
    public ReportResponse updateReport(
            Long reportId,
            ReportRequest request
    ) {

        Report report =
                getReport(reportId);

        User reportedUser =
                userRepo.findById(
                        request.getReportedUserId()
                ).orElseThrow(
                        () -> new GlobalException(
                                400,
                                "User not found"
                        )
                );

        reportMapper.updateEntity(
                request,
                report
        );

        report.setReportedUser(
                reportedUser
        );

        report =
                reportRepo.save(
                        report
                );

        return reportMapper.toResponse(
                report
        );
    }

    @Override
    public ReportResponse getReportById(
            Long reportId
    ) {
        return reportMapper.toResponse(
                getReport(reportId)
        );
    }

    @Override
    public List<ReportResponse> getAllReports() {

        return reportRepo.findAll()
                .stream()
                .map(reportMapper::toResponse)
                .toList();
    }

    @Override
    public Void deleteReport(
            Long reportId
    ) {

        Report report =
                getReport(reportId);

        reportRepo.delete(report);

        return null;
    }

    @Override
    public ReportResponse changeReportStatus(
            Long reportId,
            ReportStatusRequest request
    ) {

        Report report = getReport(reportId);

        report.setReportStatus(
                request.getReportStatus()
        );

        report.setAdminResponse(
                request.getAdminResponse()
        );

        Report saved =
                reportRepo.save(report);

        return reportMapper.toResponse(saved);
    }

    private Report getReport(
            Long reportId
    ) {

        return reportRepo.findById(reportId)
                .orElseThrow(
                        () -> new GlobalException(
                                400,
                                "Report not found"
                        )
                );
    }
}