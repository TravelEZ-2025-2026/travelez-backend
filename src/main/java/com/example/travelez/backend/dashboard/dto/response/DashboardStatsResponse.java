package com.example.travelez.backend.dashboard.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DashboardStatsResponse {
    private StatDetail totalUsers;
    private StatDetail newContent;
    private StatDetail pendingReports;
    private StatDetail lockedAccounts;

    @Data
    @Builder
    public static class StatDetail {
        private long count;
        private Integer growthPercent;
    }
}
