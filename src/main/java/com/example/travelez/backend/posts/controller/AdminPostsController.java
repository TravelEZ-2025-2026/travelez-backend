package com.example.travelez.backend.posts.controller;

import com.example.travelez.backend.common.api.BaseResponse;
import com.example.travelez.backend.common.api.CommonPage;
import com.example.travelez.backend.common.api.ResultCode;
import com.example.travelez.backend.common.dto.PaginationRequest;
import com.example.travelez.backend.common.utils.PaginationUtils;
import com.example.travelez.backend.posts.dto.request.AdminPostsFilterRequest;
import com.example.travelez.backend.posts.dto.request.BanPostRequest;
import com.example.travelez.backend.posts.dto.request.PostStatRequest;
import com.example.travelez.backend.posts.dto.request.UnbanPostRequest;
import com.example.travelez.backend.posts.dto.response.AdminPostsResponse;
import com.example.travelez.backend.posts.dto.response.PostStatResponse;
import com.example.travelez.backend.posts.service.AdminPostsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/posts")
@RequiredArgsConstructor
@Tag(name = "Admin POSTS Management", description = "Admin POSTS endpoints")
public class AdminPostsController {

    private final AdminPostsService adminPostsService;

    @Operation(summary = "Thống kê tổng quan (Top POI, Top Tags)")
    @GetMapping("/stats")
    public ResponseEntity<BaseResponse<PostStatResponse>> getStats(@ParameterObject PostStatRequest request) {
        return BaseResponse.success(adminPostsService.getStatistics(request), ResultCode.SUCCESS, "Stats fetched successfully");
    }

    @Operation(summary = "Lấy danh sách bài viết")
    @GetMapping("/list")
    public ResponseEntity<BaseResponse<CommonPage<AdminPostsResponse>>> getList(@ParameterObject AdminPostsFilterRequest request,
                                                                                @RequestParam(required = false, defaultValue = "id") String sortField,
                                                                                @RequestParam(required = false, defaultValue = "DESC") Sort.Direction sortDirection,
                                                                                @RequestParam(required = false, defaultValue = "0") Integer page,
                                                                                @RequestParam(required = false, defaultValue = "10") Integer size) {

        final PaginationRequest pageable = new PaginationRequest(page, size, sortField, sortDirection);
        return BaseResponse.success(adminPostsService.getAllPosts(request, PaginationUtils.getPageable(pageable)), ResultCode.SUCCESS, "List fetched successfully");
    }
    
    @Operation(summary = "Ban bài viết")
    @PostMapping("/{postId}/ban")
    public ResponseEntity<BaseResponse<Void>> banPost(@PathVariable Long postId, @RequestBody @Valid BanPostRequest request) {
        adminPostsService.banPost(postId, request);
        return BaseResponse.success(null, ResultCode.SUCCESS, "Post banned successfully");
    }

    @Operation(summary = "Unban bài viết")
    @PostMapping("/{postId}/unban")
    public ResponseEntity<BaseResponse<Void>> unbanPost(@PathVariable Long postId, @RequestBody @Valid UnbanPostRequest request) {
        adminPostsService.unbanPost(postId, request);
        return BaseResponse.success(null, ResultCode.SUCCESS, "Post unbanned successfully");
    }

}
