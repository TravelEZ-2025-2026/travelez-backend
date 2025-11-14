package com.example.travelez.backend.users.controller;

import com.example.travelez.backend.users.dto.response.UserDetailResponse;
import com.example.travelez.backend.users.mapper.UserMapper;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.travelez.backend.common.api.ApiResponse;
import com.example.travelez.backend.common.api.ResultCode;
import com.example.travelez.backend.security.component.UserPrinciple;
import com.example.travelez.backend.users.service.UserService;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Tag(name = "User", description = "User endpoints")
public class UserController {

    private final UserService userService;

    private final UserMapper userMapper;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserDetailResponse>> getUserById() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserPrinciple userPrinciple = (UserPrinciple) authentication.getPrincipal();

        UserDetailResponse response = userMapper
                .toUserDetailResponse(userService.getUserById(userPrinciple.getUserId()));
        return ResponseEntity.status(ResultCode.SUCCESS.getCode())
                .body(ApiResponse.success(response, "User fetched successfully"));
    }

}
