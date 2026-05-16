package com.example.travelez.backend.users.controller;

import com.example.travelez.backend.common.api.BaseResponse;
import com.example.travelez.backend.common.api.CommonPage;
import com.example.travelez.backend.common.api.ResultCode;
import com.example.travelez.backend.common.dto.PaginationRequest;
import com.example.travelez.backend.common.utils.FileUtils;
import com.example.travelez.backend.common.utils.PaginationUtils;
import com.example.travelez.backend.common.utils.SecurityUtils;
import com.example.travelez.backend.media.dto.response.MediaBaseResponse;
import com.example.travelez.backend.security.component.UserPrinciple;
import com.example.travelez.backend.users.dto.response.UserDetailResponse;
import com.example.travelez.backend.users.mapper.UserMapper;
import com.example.travelez.backend.users.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.example.travelez.backend.users.dto.request.UserUpdateRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User", description = "User endpoints")
public class UserController {

    private final UserService userService;

    @GetMapping("/search")
    public ResponseEntity<BaseResponse<CommonPage<UserDetailResponse>>> searchUsers(
            @RequestParam("keyword") String keyword,
            @RequestParam(value = "sortField", required = false, defaultValue = "id") String sortField,
            @RequestParam(value = "sortDirection", required = false, defaultValue = "ASC") Sort.Direction sortDirection,
            @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
            @RequestParam(value = "size", required = false, defaultValue = "4") Integer size
    ) {
        final PaginationRequest paginationRequest = new PaginationRequest(page, size, sortField, sortDirection);
        CommonPage<UserDetailResponse> response = userService.searchUsers(keyword, PaginationUtils.getPageable(paginationRequest));
        return BaseResponse.success(response, ResultCode.SUCCESS, "Users fetched successfully");
    }

    @GetMapping("/me")
    public ResponseEntity<BaseResponse<UserDetailResponse>> getUserById() {

        UserDetailResponse response = userService.getUserProfileById(SecurityUtils.getCurrentUserId());

        return BaseResponse.success(response, ResultCode.SUCCESS, "User fetched successfully");
    }

    @GetMapping("/{userId}")
    public ResponseEntity<BaseResponse<UserDetailResponse>> getUserProfileById(@PathVariable("userId") Long userId) {
        UserDetailResponse response = userService.getUserProfileById(userId);
        return BaseResponse.success(response, ResultCode.SUCCESS, "User fetched successfully");
    }

    @PatchMapping(value = "/me/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BaseResponse<MediaBaseResponse>> updateUserAvatar(@RequestParam("file") MultipartFile file) {
        // check file is image
        if (!FileUtils.isImage(file)) {
            return BaseResponse.failed(null, ResultCode.BAD_REQUEST, "File is not an image");
        }
        MediaBaseResponse response = userService.updateUserAvatar(file);
        return BaseResponse.success(response, ResultCode.SUCCESS, "User avatar updated successfully");
    }

    @PatchMapping(value = "/me/cover", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BaseResponse<MediaBaseResponse>> updateUserCover(@RequestParam("file") MultipartFile file) {
        // check file is image
        if (!FileUtils.isImage(file)) {
            return BaseResponse.failed(null, ResultCode.BAD_REQUEST, "File is not an image");
        }
        MediaBaseResponse response = userService.updateUserCover(file);
        return BaseResponse.success(response, ResultCode.SUCCESS, "User cover updated successfully");
    }

    @PatchMapping("/me")
    public ResponseEntity<BaseResponse<UserDetailResponse>> updateUserInfo(@Valid @RequestBody UserUpdateRequest request) {
        UserDetailResponse response = userService.updateUserInfo(request);
        return BaseResponse.success(response, ResultCode.SUCCESS, "User information updated successfully");
    }
}
