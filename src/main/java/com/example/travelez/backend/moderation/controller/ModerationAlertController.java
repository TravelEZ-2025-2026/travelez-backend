package com.example.travelez.backend.moderation.controller;

import com.example.travelez.backend.common.api.BaseResponse;
import com.example.travelez.backend.common.api.CommonPage;
import com.example.travelez.backend.common.api.ResultCode;
import com.example.travelez.backend.common.api.doc.ApiAuthResponses;
import com.example.travelez.backend.common.api.doc.ApiBaseResponses;
import com.example.travelez.backend.common.api.doc.ApiNotFoundResponses;
import com.example.travelez.backend.common.utils.PaginationUtils;
import com.example.travelez.backend.moderation.dto.request.AlertReviewRequest;
import com.example.travelez.backend.moderation.dto.request.ModerationAlertSearchRequest;
import com.example.travelez.backend.moderation.dto.response.ModerationAlertResponse;
import com.example.travelez.backend.moderation.service.ModerationAlertService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/moderation/alerts")
@RequiredArgsConstructor
@Tag(name = "Moderation Alerts", description = "Moderation alert management endpoints")
@PreAuthorize("hasRole('ADMIN')")
public class ModerationAlertController {

    private final ModerationAlertService alertService;

    @Operation(summary = "Search moderation alerts", description = "Search alerts with filters and pagination")
    @ApiBaseResponses
    @ApiAuthResponses
    @ApiResponse(responseCode = "200", description = "Alerts fetched successfully")
    @GetMapping
    public ResponseEntity<BaseResponse<CommonPage<ModerationAlertResponse>>> searchAlerts(
            @ParameterObject ModerationAlertSearchRequest request) {
        CommonPage<ModerationAlertResponse> response = alertService.searchAlerts(
                request,
                PaginationUtils.getPageable(request)
        );
        return BaseResponse.success(response, ResultCode.SUCCESS, "Alerts fetched successfully");
    }

    @Operation(summary = "Get alert by ID", description = "Get detailed information of a moderation alert")
    @ApiBaseResponses
    @ApiAuthResponses
    @ApiNotFoundResponses
    @ApiResponse(responseCode = "200", description = "Alert fetched successfully")
    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<ModerationAlertResponse>> getAlertById(@PathVariable Long id) {
        ModerationAlertResponse response = alertService.getAlertById(id);
        return BaseResponse.success(response, ResultCode.SUCCESS, "Alert fetched successfully");
    }

    @Operation(summary = "Approve alert", description = "Approve the alert and set content back to PUBLISHED")
    @ApiBaseResponses
    @ApiAuthResponses
    @ApiNotFoundResponses
    @ApiResponse(responseCode = "200", description = "Alert approved successfully")
    @PostMapping("/{id}/approve")
    public ResponseEntity<BaseResponse<Void>> approveAlert(
            @PathVariable Long id,
            @RequestBody @Valid AlertReviewRequest request) {
        alertService.approveAlert(id, request);
        return BaseResponse.success(null, ResultCode.SUCCESS, "Alert approved successfully");
    }

    @Operation(summary = "Ban alert", description = "Ban the alert and set content to BANNED")
    @ApiBaseResponses
    @ApiAuthResponses
    @ApiNotFoundResponses
    @ApiResponse(responseCode = "200", description = "Alert banned successfully")
    @PostMapping("/{id}/ban")
    public ResponseEntity<BaseResponse<Void>> banAlert(
            @PathVariable Long id,
            @RequestBody @Valid AlertReviewRequest request) {
        alertService.banAlert(id, request);
        return BaseResponse.success(null, ResultCode.SUCCESS, "Alert banned successfully");
    }
}
