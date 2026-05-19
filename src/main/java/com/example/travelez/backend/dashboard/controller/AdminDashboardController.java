package com.example.travelez.backend.dashboard.controller;

import com.example.travelez.backend.common.api.BaseResponse;
import com.example.travelez.backend.common.api.CommonPage;
import com.example.travelez.backend.common.api.ResultCode;
import com.example.travelez.backend.common.dto.PaginationRequest;
import com.example.travelez.backend.common.utils.PaginationUtils;
import com.example.travelez.backend.dashboard.dto.response.DashboardActivityResponse;
import com.example.travelez.backend.dashboard.dto.response.DashboardStatsResponse;
import com.example.travelez.backend.dashboard.service.AdminDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
@Tag(name = "Admin Dashboard", description = "Endpoints phục vụ giao diện xem tổng quan Dashboard cho admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminDashboardController {
    private final AdminDashboardService dashboardService;

    @GetMapping("/stats")
    public ResponseEntity<BaseResponse<DashboardStatsResponse>> getStats() {
        DashboardStatsResponse stats = dashboardService.getDashboardStats();
        return BaseResponse.success(stats, ResultCode.SUCCESS, "Stats fetched successfully");
    }

    @GetMapping("/activities")
    public ResponseEntity<BaseResponse<CommonPage<DashboardActivityResponse>>> getActivities(
            @RequestParam(required = false, defaultValue = "ALL") String filter,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size) {

        final PaginationRequest paginationRequest = new PaginationRequest(page, size, "createdAt", Sort.Direction.DESC);

        CommonPage<DashboardActivityResponse> response = dashboardService.getRecentActivities(
                filter,
                PaginationUtils.getPageable(paginationRequest)
        );

        return BaseResponse.success(response, ResultCode.SUCCESS, "Activities fetched successfully");
    }
}
