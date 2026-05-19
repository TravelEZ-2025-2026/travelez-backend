package com.example.travelez.backend.dashboard.service.impl;

import com.example.travelez.backend.common.api.CommonPage;
import com.example.travelez.backend.dashboard.dto.response.DashboardActivityResponse;
import com.example.travelez.backend.dashboard.dto.response.DashboardStatsResponse;
import com.example.travelez.backend.dashboard.model.SystemActivityLog;
import com.example.travelez.backend.dashboard.repository.SystemActivityLogRepository;
import com.example.travelez.backend.dashboard.service.AdminDashboardService;
import com.example.travelez.backend.posts.repository.PostsRepository;
import com.example.travelez.backend.report.model.enums.ReportStatus;
import com.example.travelez.backend.report.repository.ReportRepository;
import com.example.travelez.backend.users.model.enums.UserStatus;
import com.example.travelez.backend.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminDashboardServiceImpl implements AdminDashboardService {
    private final UserRepository userRepository;
    private final ReportRepository reportRepository;
    private final PostsRepository postsRepository;
    private final SystemActivityLogRepository activityLogRepository;

    @Override
    public DashboardStatsResponse getDashboardStats() {
        long totalUsersCount = userRepository.count();

        long lockedUsersCount = userRepository.countByStatus(UserStatus.BANNED);

        long pendingReportsCount = reportRepository.countDistinctPostsByStatus(ReportStatus.PENDING);

        long newPostsCount = postsRepository.count();

        // -----------------------------------------------------------------
        // MỐC THỜI GIAN ĐỂ TÍNH TOÁN
        // -----------------------------------------------------------------
        YearMonth currentMonth = YearMonth.now();
        YearMonth lastMonth = currentMonth.minusMonths(1);

        LocalDateTime startOfCurrentMonth = currentMonth.atDay(1).atStartOfDay();
        LocalDateTime endOfCurrentMonth = currentMonth.atEndOfMonth().atTime(23, 59, 59);

        LocalDateTime startOfLastMonth = lastMonth.atDay(1).atStartOfDay();
        LocalDateTime endOfLastMonth = lastMonth.atEndOfMonth().atTime(23, 59, 59);

        // -----------------------------------------------------------------
        // GROWTH PERCENT CHO TOTAL USERS
        // -----------------------------------------------------------------
        long currentMonthUsers = userRepository.countByCreatedAtBetween(startOfCurrentMonth, endOfCurrentMonth);
        long totalUsersUntilLastMonth = totalUsersCount - currentMonthUsers;

        Integer userGrowthPercent = null;
        if (totalUsersUntilLastMonth >= 0) {
            double growth = ((double) currentMonthUsers / totalUsersUntilLastMonth) * 100;
            userGrowthPercent = (int) Math.round(growth);
        } else if (totalUsersUntilLastMonth == 0 && currentMonthUsers > 0){
            userGrowthPercent = 100;
        }
        else {
            userGrowthPercent = 0;
        }

        // -----------------------------------------------------------------
        // GROWTH PERCENT CHO NEW CONTENT (POSTS)
        // -----------------------------------------------------------------
        long currentMonthPosts = postsRepository.countByCreatedAtBetween(startOfCurrentMonth, endOfCurrentMonth);
        long lastMonthPosts = postsRepository.countByCreatedAtBetween(startOfLastMonth, endOfLastMonth);

        Integer postsGrowthPercent = null;
        if (lastMonthPosts > 0) {
            double growth = ((double) (currentMonthPosts - lastMonthPosts) / lastMonthPosts) * 100;
            postsGrowthPercent = (int) Math.round(growth);
        } else if (currentMonthPosts > 0) {
            postsGrowthPercent = 100;
        }
        else {
            postsGrowthPercent = 0;
        }

        // -----------------------------------------------------------------
        // BUILD RESPONSE
        // -----------------------------------------------------------------
        return DashboardStatsResponse.builder()
                .totalUsers(DashboardStatsResponse.StatDetail.builder()
                        .count(totalUsersCount)
                        .growthPercent(userGrowthPercent)
                        .build())
                .lockedAccounts(DashboardStatsResponse.StatDetail.builder().count(lockedUsersCount).build())
                .pendingReports(DashboardStatsResponse.StatDetail.builder().count(pendingReportsCount).build())
                .newContent(DashboardStatsResponse.StatDetail.builder()
                        .count(newPostsCount)
                        .growthPercent(postsGrowthPercent)
                        .build())
                .build();
    }

    @Override
    public CommonPage<DashboardActivityResponse> getRecentActivities(String filter, Pageable pageable) {
        Page<SystemActivityLog> logPage = activityLogRepository.findRecentActivities(filter, pageable);

        List<DashboardActivityResponse> responses = logPage.getContent().stream()
                .map(log -> DashboardActivityResponse.builder()
                        .time(log.getCreatedAt())
                        .category(log.getCategory().name())
                        .description(log.getDescription())
                        .status(log.getStatus())
                        .build())
                .toList();

        return new CommonPage<>(responses, logPage.getTotalPages(), logPage.getTotalElements(),
                pageable.getPageSize(), logPage.getNumber(), logPage.isEmpty());
    }
}
