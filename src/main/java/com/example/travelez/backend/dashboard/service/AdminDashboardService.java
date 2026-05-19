package com.example.travelez.backend.dashboard.service;

import com.example.travelez.backend.common.api.CommonPage;
import com.example.travelez.backend.dashboard.dto.response.DashboardActivityResponse;
import com.example.travelez.backend.dashboard.dto.response.DashboardStatsResponse;
import org.springframework.data.domain.Pageable;

public interface AdminDashboardService {
    DashboardStatsResponse getDashboardStats();
    CommonPage<DashboardActivityResponse> getRecentActivities(String filter, Pageable pageable);
}
