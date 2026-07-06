package com.example.AiTaster.service;

import com.example.AiTaster.Util.PageUtil;
import com.example.AiTaster.constant.ErrorCode;
import com.example.AiTaster.constant.ProjectStatus;
import com.example.AiTaster.constant.Role;
import com.example.AiTaster.constant.UserStatus;
import com.example.AiTaster.dto.request.AdminDashboard.AdminRecentAccountFilterRequest;
import com.example.AiTaster.dto.response.*;
import com.example.AiTaster.entity.User;
import com.example.AiTaster.exception.GlobalException;
import com.example.AiTaster.repository.ExpertApplicationRepo;
import com.example.AiTaster.repository.ProjectRepo;
import com.example.AiTaster.repository.UserRepo;
import com.example.AiTaster.service.imp.IAdminDashboardService;
import com.example.AiTaster.specification.AdminRecentAccountSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.Year;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminDashboardService implements IAdminDashboardService {

    private final UserRepo userRepo;

    private final ProjectRepo projectRepo;

    private final ExpertApplicationRepo expertApplicationRepo;

    private final CurrentUserService currentUserService;

    @Override
    @Transactional(readOnly = true)
    public AdminDashboardSummaryResponse getSummary() {
        checkAdmin();

        return AdminDashboardSummaryResponse.builder()
                .users(buildUserSummary())
                .projects(buildProjectSummary())
                .applications(buildApplicationSummary())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public UserGrowthResponse getUserGrowth(Integer year) {
        checkAdmin();

        int targetYear =
                year != null ? year : Year.now().getValue();

        YearMonth startMonth =
                YearMonth.of(targetYear, 1);

        YearMonth endMonth =
                YearMonth.of(targetYear, 12);

        LocalDateTime startDate =
                startMonth.atDay(1).atStartOfDay();

        LocalDateTime endDate =
                endMonth.atEndOfMonth().atTime(23, 59, 59);

        List<User> usersInRange =
                userRepo.findByRoleInAndCreateAtBetween(
                        List.of(Role.CLIENT, Role.EXPERT),
                        startDate,
                        endDate
                );

        Map<YearMonth, Long> monthlyClientCounts =
                new HashMap<>();

        Map<YearMonth, Long> monthlyExpertCounts =
                new HashMap<>();

        for (User user : usersInRange) {
            if (user.getCreateAt() == null) {
                continue;
            }

            YearMonth userCreatedMonth =
                    YearMonth.from(user.getCreateAt());

            if (Role.CLIENT.equals(user.getRole())) {
                monthlyClientCounts.merge(
                        userCreatedMonth,
                        1L,
                        Long::sum
                );
            }

            if (Role.EXPERT.equals(user.getRole())) {
                monthlyExpertCounts.merge(
                        userCreatedMonth,
                        1L,
                        Long::sum
                );
            }
        }

        long clientsBeforeStart =
                userRepo.countByRoleAndCreateAtBefore(
                        Role.CLIENT,
                        startDate
                );

        long expertsBeforeStart =
                userRepo.countByRoleAndCreateAtBefore(
                        Role.EXPERT,
                        startDate
                );

        List<String> labels =
                new ArrayList<>();

        List<Long> clients =
                new ArrayList<>();

        List<Long> experts =
                new ArrayList<>();

        long runningClientTotal =
                clientsBeforeStart;

        long runningExpertTotal =
                expertsBeforeStart;

        for (int monthIndex = 1; monthIndex <= 12; monthIndex++) {
            YearMonth month =
                    YearMonth.of(targetYear, monthIndex);

            runningClientTotal +=
                    monthlyClientCounts.getOrDefault(month, 0L);

            runningExpertTotal +=
                    monthlyExpertCounts.getOrDefault(month, 0L);

            labels.add(
                    month.getMonth()
                            .getDisplayName(
                                    TextStyle.SHORT,
                                    Locale.ENGLISH
                            )
            );

            clients.add(runningClientTotal);
            experts.add(runningExpertTotal);
        }

        return UserGrowthResponse.builder()
                .labels(labels)
                .clients(clients)
                .experts(experts)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AdminRecentAccountResponse> getRecentAccounts(
            AdminRecentAccountFilterRequest request
    ) {
        checkAdmin();

        Pageable pageable =
                PageUtil.createPageable(request);

        Page<AdminRecentAccountResponse> page =
                userRepo.findAll(
                                AdminRecentAccountSpecification.filter(request),
                                pageable
                        )
                        .map(this::toAdminRecentAccountResponse);

        return PageResponse.fromPage(page);
    }

    private AdminRecentAccountResponse toAdminRecentAccountResponse(User user) {
        return AdminRecentAccountResponse.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .avatarUrl(user.getAvatarUrl())
                .phone(user.getPhone())
                .role(user.getRole())
                .userStatus(user.getUserStatus())
                .createAt(user.getCreateAt())
                .build();
    }
    private RecentUserResponse toRecentUserResponse(User user) {
        return RecentUserResponse.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .avatarUrl(user.getAvatarUrl())
                .role(user.getRole())
                .userStatus(user.getUserStatus())
                .createAt(user.getCreateAt())
                .build();
    }

    private int normalizeRecentMinutes(int minutes) {
        if (minutes < 1) {
            return 30;
        }

        if (minutes > 1440) {
            return 1440;
        }

        return minutes;
    }

    private int normalizeRecentLimit(int limit) {
        if (limit < 1) {
            return 10;
        }

        if (limit > 50) {
            return 50;
        }

        return limit;
    }
    private int normalizeMonths(int months) {
        if (months < 1) {
            return 7;
        }

        if (months > 24) {
            return 24;
        }

        return months;
    }

    private AdminDashboardSummaryResponse.UserSummary buildUserSummary() {
        return AdminDashboardSummaryResponse.UserSummary.builder()
                .total(userRepo.count())
                .clients(userRepo.countByRole(Role.CLIENT))
                .experts(userRepo.countByRole(Role.EXPERT))
                .admins(userRepo.countByRole(Role.ADMIN))
                .active(userRepo.countByUserStatus(UserStatus.ACTIVE))
                .build();
    }

    private AdminDashboardSummaryResponse.ProjectSummary buildProjectSummary() {
        Map<String, Long> statusCounts =
                Arrays.stream(ProjectStatus.values())
                        .collect(Collectors.toMap(
                                status -> status.name().toLowerCase(),
                                projectRepo::countByProjectStatus,
                                (oldValue, newValue) -> oldValue,
                                LinkedHashMap::new
                        ));

        return AdminDashboardSummaryResponse.ProjectSummary.builder()
                .total(projectRepo.count())
                .statuses(statusCounts)
                .build();
    }

    private AdminDashboardSummaryResponse.ApplicationSummary buildApplicationSummary() {
        return AdminDashboardSummaryResponse.ApplicationSummary.builder()
                .total(expertApplicationRepo.count())
                .build();
    }

    private void checkAdmin() {
        User currentUser = currentUserService.getCurrentUser();

        if (!Role.ADMIN.equals(currentUser.getRole())) {
            throw new GlobalException(ErrorCode.INVALID_ROLE);
        }
    }
}