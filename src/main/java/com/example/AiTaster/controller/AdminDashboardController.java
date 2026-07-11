package com.example.AiTaster.controller;

import com.example.AiTaster.dto.request.AdminDashboard.AdminRecentAccountFilterRequest;
import com.example.AiTaster.dto.response.*;
import com.example.AiTaster.service.AdminDashboardService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
@SecurityRequirement(name = "api")
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    @GetMapping("/summary")
    public ResponseEntity<APIResponse<AdminDashboardSummaryResponse>> getSummary() {
        AdminDashboardSummaryResponse response =
                adminDashboardService.getSummary();

        return ResponseEntity.ok(
                APIResponse.response(
                        200,
                        "Lấy tổng quan bảng điều khiển quản trị thành công",
                        response
                )
        );
    }

    @GetMapping("/user-growth")
    public ResponseEntity<APIResponse<UserGrowthResponse>> getUserGrowth(
            @RequestParam(required = false) Integer year
    ) {
        UserGrowthResponse response =
                adminDashboardService.getUserGrowth(year);

        return ResponseEntity.ok(
                APIResponse.response(
                        200,
                        "Lấy tăng trưởng người dùng thành công",
                        response
                )
        );
    }

    @PostMapping("/recent-accounts/filter")
    public ResponseEntity<APIResponse<PageResponse<AdminRecentAccountResponse>>> getRecentAccounts(
            @RequestBody(required = false) @Valid AdminRecentAccountFilterRequest request
    ) {
        PageResponse<AdminRecentAccountResponse> response =
                adminDashboardService.getRecentAccounts(request);

        return ResponseEntity.ok(
                APIResponse.response(
                        200,
                        "Lấy tài khoản gần đây thành công",
                        response
                )
        );
    }
}
