package com.example.AiTaster.controller;

import com.example.AiTaster.constant.ReportStatus;
import com.example.AiTaster.dto.request.ReportRequest;
import com.example.AiTaster.dto.response.APIResponse;
import com.example.AiTaster.dto.response.ReportResponse;
import com.example.AiTaster.service.ReportService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin("*")
@SecurityRequirement(name = "api")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<APIResponse<ReportResponse>> createReport(
            @ModelAttribute @Valid ReportRequest request
    ) {
        return ResponseEntity.status(201).body(
                APIResponse.response(
                        201,
                        "Tạo báo cáo thành công",
                        reportService.createReport(request)
                )
        );
    }

    @GetMapping("/my")
    public ResponseEntity<APIResponse<List<ReportResponse>>> getMyReports() {
        return ResponseEntity.ok(
                APIResponse.response(
                        200,
                        "Lấy báo cáo của tôi thành công",
                        reportService.getMyReports()
                )
        );
    }

    @GetMapping("/{reportId}")
    public ResponseEntity<APIResponse<ReportResponse>> getReport(
            @PathVariable Long reportId
    ) {
        return ResponseEntity.ok(
                APIResponse.response(
                        200,
                        "Lấy báo cáo thành công",
                        reportService.getReportById(reportId)
                )
        );
    }

    @GetMapping
    public ResponseEntity<APIResponse<List<ReportResponse>>> getAllReports() {
        return ResponseEntity.ok(
                APIResponse.response(
                        200,
                        "Lấy danh sách báo cáo thành công",
                        reportService.getAllReports()
                )
        );
    }

    @PutMapping(
            value = "/{reportId}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<APIResponse<ReportResponse>> updateReport(
            @PathVariable Long reportId,
            @ModelAttribute @Valid ReportRequest request
    ) {
        return ResponseEntity.ok(
                APIResponse.response(
                        200,
                        "Cập nhật báo cáo thành công",
                        reportService.updateReport(reportId, request)
                )
        );
    }

    @PatchMapping("/{reportId}/status")
    public ResponseEntity<APIResponse<ReportResponse>> changeReportStatus(
            @PathVariable Long reportId,
            @RequestParam ReportStatus reportStatus,
            @RequestParam(required = false) String adminResponse
    ) {
        return ResponseEntity.ok(
                APIResponse.response(
                        200,
                        "Cập nhật trạng thái báo cáo thành công",
                        reportService.changeReportStatus(
                                reportId,
                                reportStatus,
                                adminResponse
                        )
                )
        );
    }

    @DeleteMapping("/{reportId}")
    public ResponseEntity<APIResponse<Void>> deleteReport(
            @PathVariable Long reportId
    ) {
        reportService.deleteReport(reportId);

        return ResponseEntity.ok(
                APIResponse.response(
                        200,
                        "Xóa báo cáo thành công",
                        null
                )
        );
    }
}
