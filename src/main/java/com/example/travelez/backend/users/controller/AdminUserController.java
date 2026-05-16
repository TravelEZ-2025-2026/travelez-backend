package com.example.travelez.backend.users.controller;

import com.example.travelez.backend.common.api.BaseResponse;
import com.example.travelez.backend.common.api.CommonPage;
import com.example.travelez.backend.common.api.ResultCode;
import com.example.travelez.backend.common.dto.PaginationRequest;
import com.example.travelez.backend.common.utils.PaginationUtils;
import com.example.travelez.backend.users.dto.request.AdminUserFilterRequest;
import com.example.travelez.backend.users.dto.request.UserStatusUpdateRequest;
import com.example.travelez.backend.users.dto.response.AdminUserResponse;
import com.example.travelez.backend.users.dto.response.UserAdminDetailResponse;
import com.example.travelez.backend.users.service.AdminUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@Tag(name = "Admin - User Management", description = "Admin endpoints for user management")
public class AdminUserController {

    private final AdminUserService adminUserService;

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Get all users",
        description = "Get paginated list of all TRAVELER users with optional filters (status, keyword search)"
    )
    @ApiResponse(responseCode = "200", description = "Users fetched successfully")
    @GetMapping
    public ResponseEntity<BaseResponse<CommonPage<AdminUserResponse>>> getAllUsers(
            @ModelAttribute AdminUserFilterRequest filter,
            @RequestParam(value = "sortField", required = false, defaultValue = "createdAt") String sortField,
            @RequestParam(value = "sortDirection", required = false, defaultValue = "DESC") Sort.Direction sortDirection,
            @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
            @RequestParam(value = "size", required = false, defaultValue = "10") Integer size
    ) {
        final PaginationRequest paginationRequest = new PaginationRequest(page, size, sortField, sortDirection);
        CommonPage<AdminUserResponse> response = adminUserService.getAllUsersForAdmin(
            filter, 
            PaginationUtils.getPageable(paginationRequest)
        );
        return BaseResponse.success(response, ResultCode.SUCCESS, "Users fetched successfully");
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Get user detail",
        description = "Get detailed information of a TRAVELER user by ID"
    )
    @ApiResponse(responseCode = "200", description = "User detail fetched successfully")
    @ApiResponse(responseCode = "404", description = "User not found")
    @ApiResponse(responseCode = "403", description = "Can only view TRAVELER users")
    @GetMapping("/{userId}")
    public ResponseEntity<BaseResponse<UserAdminDetailResponse>> getUserDetail(@PathVariable Long userId) {
        UserAdminDetailResponse response = adminUserService.getUserDetail(userId);
        return BaseResponse.success(response, ResultCode.SUCCESS, "User detail fetched successfully");
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Update user status (Ban/Unban)",
        description = "Ban or unban a TRAVELER user. Only affects users with TRAVELER role."
    )
    @ApiResponse(responseCode = "200", description = "User status updated successfully")
    @ApiResponse(responseCode = "404", description = "User not found")
    @ApiResponse(responseCode = "403", description = "Can only update TRAVELER users")
    @PatchMapping("/{userId}/status")
    public ResponseEntity<BaseResponse<Void>> updateUserStatus(
            @PathVariable Long userId,
            @RequestBody @Valid UserStatusUpdateRequest request) {
        adminUserService.updateUserStatus(userId, request);
        return BaseResponse.success(null, ResultCode.SUCCESS, "User status updated successfully");
    }
}
