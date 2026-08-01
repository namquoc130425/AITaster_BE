package com.example.AiTaster.service;

import com.example.AiTaster.constant.Role;
import com.example.AiTaster.dto.response.UserGrowthResponse;
import com.example.AiTaster.entity.User;
import com.example.AiTaster.repository.ExpertApplicationRepo;
import com.example.AiTaster.repository.ProjectRepo;
import com.example.AiTaster.repository.UserRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Year;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminDashboardServiceTest {

    @Mock
    private UserRepo userRepo;
    @Mock
    private ProjectRepo projectRepo;
    @Mock
    private ExpertApplicationRepo expertApplicationRepo;
    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private AdminDashboardService adminDashboardService;

    @Test
    void getUserGrowthStopsAtCurrentMonthForCurrentYear() {
        mockAdminAndEmptyGrowthData();

        UserGrowthResponse result = adminDashboardService.getUserGrowth(
                Year.now().getValue()
        );

        int currentMonth = YearMonth.now().getMonthValue();
        String currentMonthLabel = YearMonth.now()
                .getMonth()
                .getDisplayName(TextStyle.SHORT, Locale.ENGLISH);

        assertThat(result.getLabels()).hasSize(currentMonth);
        assertThat(result.getLabels().get(currentMonth - 1))
                .isEqualTo(currentMonthLabel);
        assertThat(result.getClients()).hasSize(currentMonth);
        assertThat(result.getExperts()).hasSize(currentMonth);
    }

    @Test
    void getUserGrowthKeepsAllTwelveMonthsForPastYear() {
        mockAdminAndEmptyGrowthData();

        UserGrowthResponse result = adminDashboardService.getUserGrowth(
                Year.now().minusYears(1).getValue()
        );

        assertThat(result.getLabels()).hasSize(12);
        assertThat(result.getLabels()).endsWith("Dec");
        assertThat(result.getClients()).hasSize(12);
        assertThat(result.getExperts()).hasSize(12);
    }

    private void mockAdminAndEmptyGrowthData() {
        when(currentUserService.getCurrentUser())
                .thenReturn(User.builder().role(Role.ADMIN).build());
        when(userRepo.findByRoleInAndCreateAtBetween(
                anyList(),
                any(),
                any()
        )).thenReturn(List.of());
        when(userRepo.countByRoleAndCreateAtBefore(any(), any()))
                .thenReturn(0L);
    }
}
