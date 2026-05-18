package com.example.travelez.backend.report.controller;

import com.example.travelez.backend.report.model.enums.ReportStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.travelez.backend.common.api.BaseResponse;
import com.example.travelez.backend.common.api.CommonPage;
import com.example.travelez.backend.common.api.ResultCode;
import com.example.travelez.backend.common.utils.PaginationUtils;
import com.example.travelez.backend.report.dto.request.AdminRejectReportRequest;
import com.example.travelez.backend.report.dto.request.ReportedItemsFilterRequest;
import com.example.travelez.backend.report.dto.response.PostReportsResponse;
import com.example.travelez.backend.report.dto.response.ReportedPostResponse;
import com.example.travelez.backend.report.service.AdminReportService;

@RestController
@RequestMapping("/api/admin/reports")
@RequiredArgsConstructor
@Tag(name = "Admin Reports", description = "Admin Reports endpoints")
public class AdminReportController {
    private final AdminReportService adminReportService;

    @Operation(
        summary = "Get reported items",
        description = "Get list of posts that have been reported with report count and filters"
    )
    @ApiResponse(responseCode = "200", description = "Reported items fetched successfully")
    @GetMapping("/reported-items")
    public ResponseEntity<BaseResponse<CommonPage<ReportedPostResponse>>> getReportedItems(
            @ParameterObject ReportedItemsFilterRequest filter,
            @ParameterObject Pageable pageable) {
        CommonPage<ReportedPostResponse> response = adminReportService.getReportedItems(filter, pageable);
        return BaseResponse.success(response, ResultCode.SUCCESS, "Reported items fetched successfully");
    }

    @PostMapping("/reject")
    public ResponseEntity<BaseResponse<Void>> rejectReport(@RequestBody @Valid AdminRejectReportRequest request) {
        adminReportService.rejectReports(request);
        return BaseResponse.success(null, ResultCode.SUCCESS, "Report rejected successfully");
    }

    @Operation(
        summary = "Get post reports",
        description = "Get all reports for a specific post with optional status filter"
    )
    @ApiResponse(responseCode = "200", description = "Post reports fetched successfully")
    @ApiResponse(responseCode = "404", description = "Post not found")
    @GetMapping("/posts/{postId}")
    public ResponseEntity<BaseResponse<PostReportsResponse>> getPostReports(
            @PathVariable Long postId,
            @RequestParam(required = false) ReportStatus status,
            @ParameterObject Pageable pageable) {
        PostReportsResponse response = adminReportService.getPostReports(postId, status, pageable);
        return BaseResponse.success(response, ResultCode.SUCCESS, "Post reports fetched successfully");
    }

}
