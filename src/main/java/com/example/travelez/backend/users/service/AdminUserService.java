package com.example.travelez.backend.users.service;

import org.springframework.data.domain.Pageable;

import com.example.travelez.backend.common.api.CommonPage;
import com.example.travelez.backend.users.dto.request.AdminUserFilterRequest;
import com.example.travelez.backend.users.dto.request.UserStatusUpdateRequest;
import com.example.travelez.backend.users.dto.response.AdminUserResponse;
import com.example.travelez.backend.users.dto.response.UserAdminDetailResponse;

public interface AdminUserService {
    
    CommonPage<AdminUserResponse> getAllUsersForAdmin(AdminUserFilterRequest filter, Pageable pageable);

    UserAdminDetailResponse getUserDetail(Long userId);
    
    void updateUserStatus(Long userId, UserStatusUpdateRequest request);
}
