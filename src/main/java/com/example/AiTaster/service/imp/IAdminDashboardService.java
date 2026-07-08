package com.example.AiTaster.service.imp;

import com.example.AiTaster.dto.request.AdminDashboard.AdminRecentAccountFilterRequest;
import com.example.AiTaster.dto.response.*;

import java.util.List;

public interface IAdminDashboardService {

    AdminDashboardSummaryResponse getSummary();

    UserGrowthResponse getUserGrowth(Integer year);

    PageResponse<AdminRecentAccountResponse> getRecentAccounts(
            AdminRecentAccountFilterRequest request
    );
}