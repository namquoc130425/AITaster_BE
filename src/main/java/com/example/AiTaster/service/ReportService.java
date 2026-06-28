package com.example.AiTaster.service;

import com.example.AiTaster.constant.ErrorCode;
import com.example.AiTaster.constant.ReportStatus;
import com.example.AiTaster.constant.Role;
import com.example.AiTaster.dto.request.ReportRequest;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService implements IReportService {

    private final ReportRepo reportRepo;
    private final UserRepo userRepo;
    private final CurrentUserService currentUserService;
    private final ReportMapper reportMapper;
    private final LocalFileStorageService localFileStorageService;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public ReportResponse createReport(
            ReportRequest request
    ) {
        User reporter =
                currentUserService.getCurrentUser();

        User reportedUser =
                getUserById(request.getReportedUserId());

        if (reporter.getUserId().equals(reportedUser.getUserId())) {
            throw new GlobalException(ErrorCode.CANNOT_REPORT_YOURSELF);
        }

        Report report = reportMapper.toEntity(request);

        String evidenceUrl = localFileStorageService.saveReportEvidence(request.getEvidenceFile());

        report.setReporter(reporter);
        report.setReportedUser(reportedUser);
        report.setEvidenceFile(evidenceUrl);
        report.setReportStatus(ReportStatus.PENDING);

        Report saved = reportRepo.save(report);

        notificationService.notifyAdminNewReport(saved);

        return reportMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public ReportResponse updateReport(
            Long reportId,
            ReportRequest request
    ) {
        User currentUser = currentUserService.getCurrentUser();

        Report report = getReport(reportId);

        checkReporterOwner(report, currentUser);

        if (!ReportStatus.PENDING.equals(report.getReportStatus())) {
            throw new GlobalException(ErrorCode.CANNOT_UPDATE_REPORT);
        }

        User reportedUser =
                getUserById(request.getReportedUserId());

        if (currentUser.getUserId().equals(reportedUser.getUserId())) {
            throw new GlobalException(ErrorCode.CANNOT_REPORT_YOURSELF);
        }

        reportMapper.updateEntity(
                request,
                report
        );

        report.setReportedUser(reportedUser);

        if (request.getEvidenceFile() != null
                && !request.getEvidenceFile().isEmpty()) {

            String evidenceUrl =
                    localFileStorageService.saveReportEvidence(
                            request.getEvidenceFile()
                    );

            report.setEvidenceFile(evidenceUrl);
        }

        Report saved =
                reportRepo.save(report);

        return reportMapper.toResponse(saved);
    }

    @Override
    public ReportResponse getReportById(
            Long reportId
    ) {

        User currentUser = currentUserService.getCurrentUser();

        Report report =
                getReport(reportId);

        if (isAdmin(currentUser) || isReporter(report, currentUser)) {
            return reportMapper.toResponse(report);
        }

        throw new GlobalException(
                ErrorCode.NOT_REPORT_OWNER
        );
    }

    @Override
    public List<ReportResponse> getAllReports() {

        User currentUser = currentUserService.getCurrentUser();

        checkAdmin(currentUser);

        return reportRepo.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(reportMapper::toResponse)
                .toList();
    }

    @Override
    public List<ReportResponse> getMyReports() {

        User currentUser =
                currentUserService.getCurrentUser();

        return reportRepo.findByReporterOrderByCreatedAtDesc(
                        currentUser
                )
                .stream()
                .map(reportMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public ReportResponse changeReportStatus(
            Long reportId,
            ReportStatus reportStatus,
            String adminResponse
    ) {

        User currentUser =
                currentUserService.getCurrentUser();

        checkAdmin(currentUser);

        Report report =
                getReport(reportId);

        report.setReportStatus(reportStatus);
        report.setAdminResponse(adminResponse);

        Report saved =
                reportRepo.save(report);

        switch (reportStatus) {

            case RESOLVED ->
                    notificationService.notifyReporterReportResolved(saved);

            case REJECTED ->
                    notificationService.notifyReporterReportRejected(saved);

            default -> {
            }
        }

        return reportMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public Void deleteReport(
            Long reportId
    ) {

        User currentUser =
                currentUserService.getCurrentUser();

        checkAdmin(currentUser);

        Report report =
                getReport(reportId);

        reportRepo.delete(report);

        return null;
    }

    private Report getReport(
            Long reportId
    ) {

        return reportRepo.findById(reportId)
                .orElseThrow(() ->
                        new GlobalException(
                                ErrorCode.REPORT_NOT_FOUND
                        )
                );
    }

    private User getUserById(
            Long userId
    ) {

        return userRepo.findById(userId)
                .orElseThrow(() ->
                        new GlobalException(
                                ErrorCode.USER_NOT_FOUND
                        )
                );
    }

    private void checkReporterOwner(
            Report report,
            User currentUser
    ) {

        if (!isReporter(report, currentUser)) {
            throw new GlobalException(
                    ErrorCode.NOT_REPORT_OWNER
            );
        }
    }

    private boolean isReporter(
            Report report,
            User currentUser
    ) {

        return report.getReporter()
                .getUserId()
                .equals(currentUser.getUserId());
    }

    private boolean isAdmin(
            User user
    ) {

        return Role.ADMIN.equals(
                user.getRole()
        );
    }

    private void checkAdmin(
            User user
    ) {

        if (!isAdmin(user)) {
            throw new GlobalException(
                    ErrorCode.INVALID_ROLE
            );
        }
    }
}