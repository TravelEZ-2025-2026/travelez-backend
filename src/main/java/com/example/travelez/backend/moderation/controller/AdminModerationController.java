package com.example.travelez.backend.moderation.controller;

import com.example.travelez.backend.common.api.BaseResponse;
import com.example.travelez.backend.common.api.CommonPage;
import com.example.travelez.backend.common.api.ResultCode;
import com.example.travelez.backend.common.api.doc.ApiAuthResponses;
import com.example.travelez.backend.common.api.doc.ApiBaseResponses;
import com.example.travelez.backend.common.utils.PaginationUtils;
import com.example.travelez.backend.moderation.dto.request.BannedKeywordCreateRequest;
import com.example.travelez.backend.moderation.dto.request.BannedKeywordSearchRequest;
import com.example.travelez.backend.moderation.dto.request.BannedKeywordUpdateRequest;
import com.example.travelez.backend.moderation.dto.response.BannedKeywordResponse;
import com.example.travelez.backend.moderation.dto.response.ModerationDashboardResponse;
import com.example.travelez.backend.moderation.service.BannedKeywordService;
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
@RequestMapping("/api/admin/moderation")
@RequiredArgsConstructor
@Tag(name = "Admin Moderation", description = "Admin moderation management endpoints")
@PreAuthorize("hasRole('ADMIN')")
public class AdminModerationController {

    private final BannedKeywordService keywordService;
    private final com.example.travelez.backend.moderation.service.ModerationAlertService alertService;

    @Operation(summary = "Create banned keyword", description = "Add a new banned keyword to the system")
    @ApiBaseResponses
    @ApiAuthResponses
    @ApiResponse(responseCode = "201", description = "Keyword created successfully")
    @PostMapping("/keywords")
    public ResponseEntity<BaseResponse<Void>> createKeyword(@RequestBody @Valid BannedKeywordCreateRequest request) {
        keywordService.createKeyword(request);
        return BaseResponse.success(null, ResultCode.CREATED, "Keyword created successfully");
    }

    @Operation(summary = "Search banned keywords", description = "Search keywords with filters and pagination")
    @ApiBaseResponses
    @ApiAuthResponses
    @ApiResponse(responseCode = "200", description = "Keywords fetched successfully")
    @GetMapping("/keywords")
    public ResponseEntity<BaseResponse<CommonPage<BannedKeywordResponse>>> searchKeywords(
            @ParameterObject BannedKeywordSearchRequest request) {
        CommonPage<BannedKeywordResponse> response = keywordService.searchKeywords(
                request, 
                PaginationUtils.getPageable(request)
        );
        return BaseResponse.success(response, ResultCode.SUCCESS, "Keywords fetched successfully");
    }

    @Operation(summary = "Get keyword by ID", description = "Get detailed information of a banned keyword")
    @ApiBaseResponses
    @ApiAuthResponses
    @ApiResponse(responseCode = "200", description = "Keyword fetched successfully")
    @GetMapping("/keywords/{id}")
    public ResponseEntity<BaseResponse<BannedKeywordResponse>> getKeywordById(@PathVariable Long id) {
        BannedKeywordResponse response = keywordService.getKeywordById(id);
        return BaseResponse.success(response, ResultCode.SUCCESS, "Keyword fetched successfully");
    }

    @Operation(summary = "Update banned keyword", description = "Update an existing banned keyword")
    @ApiBaseResponses
    @ApiAuthResponses
    @ApiResponse(responseCode = "200", description = "Keyword updated successfully")
    @PatchMapping("/keywords/{id}")
    public ResponseEntity<BaseResponse<Void>> updateKeyword(
            @PathVariable Long id,
            @RequestBody @Valid BannedKeywordUpdateRequest request) {
        keywordService.updateKeyword(id, request);
        return BaseResponse.success(null, ResultCode.SUCCESS, "Keyword updated successfully");
    }

    @Operation(summary = "Delete banned keyword", description = "Delete a banned keyword from the system")
    @ApiBaseResponses
    @ApiAuthResponses
    @ApiResponse(responseCode = "200", description = "Keyword deleted successfully")
    @DeleteMapping("/keywords/{id}")
    public ResponseEntity<BaseResponse<Void>> deleteKeyword(@PathVariable Long id) {
        keywordService.deleteKeyword(id);
        return BaseResponse.success(null, ResultCode.SUCCESS, "Keyword deleted successfully");
    }

    @Operation(summary = "Toggle keyword status", description = "Enable or disable a banned keyword")
    @ApiBaseResponses
    @ApiAuthResponses
    @ApiResponse(responseCode = "200", description = "Keyword toggled successfully")
    @PatchMapping("/keywords/{id}/toggle")
    public ResponseEntity<BaseResponse<Void>> toggleKeyword(@PathVariable Long id) {
        keywordService.toggleKeyword(id);
        return BaseResponse.success(null, ResultCode.SUCCESS, "Keyword toggled successfully");
    }

    @Operation(summary = "Refresh cache", description = "Manually refresh the banned keywords cache")
    @ApiBaseResponses
    @ApiAuthResponses
    @ApiResponse(responseCode = "200", description = "Cache refreshed successfully")
    @PostMapping("/keywords/refresh-cache")
    public ResponseEntity<BaseResponse<Void>> refreshCache() {
        keywordService.refreshCache();
        return BaseResponse.success(null, ResultCode.SUCCESS, "Cache refreshed successfully");
    }

    @Operation(summary = "Get moderation dashboard", description = "Get overview statistics for moderation")
    @ApiBaseResponses
    @ApiAuthResponses
    @ApiResponse(responseCode = "200", description = "Dashboard fetched successfully")
    @GetMapping("/dashboard")
    public ResponseEntity<BaseResponse<ModerationDashboardResponse>> getDashboard() {
        ModerationDashboardResponse response = alertService.getDashboard();
        return BaseResponse.success(response, ResultCode.SUCCESS, "Dashboard fetched successfully");
    }
}
