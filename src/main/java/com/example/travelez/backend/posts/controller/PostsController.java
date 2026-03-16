package com.example.travelez.backend.posts.controller;

import com.example.travelez.backend.common.api.BaseResponse;
import com.example.travelez.backend.common.api.CommonPage;
import com.example.travelez.backend.common.api.CursorResponse;
import com.example.travelez.backend.common.api.ResultCode;
import com.example.travelez.backend.common.dto.PaginationRequest;
import com.example.travelez.backend.common.utils.PaginationUtils;
import com.example.travelez.backend.common.utils.SecurityUtils;
import com.example.travelez.backend.posts.dto.request.CursorPostsRequest;
import com.example.travelez.backend.posts.dto.request.PostsCreateRequest;
import com.example.travelez.backend.posts.dto.request.PostsSearchRequest;
import com.example.travelez.backend.posts.dto.request.PostsUpdateRequest;
import com.example.travelez.backend.posts.dto.response.PostResponse;
import com.example.travelez.backend.posts.dto.response.PostsDetailResponse;
import com.example.travelez.backend.posts.service.PostsService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
@Tag(name = "Posts", description = "Posts endpoints")
public class PostsController {
    private final PostsService postsService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BaseResponse<Void>> createPosts(@ModelAttribute @Valid PostsCreateRequest request) {
        postsService.createPost(request);
        return BaseResponse.success(null, ResultCode.CREATED, "Posts created successfully");
    }

    @GetMapping("/feeds/friends")
    public ResponseEntity<BaseResponse<CursorResponse<PostResponse>>> getAllPosts(@RequestParam(required = false, defaultValue = "4") Long size, @RequestParam(required = false) Long lastPostId) {
        CursorPostsRequest request = new CursorPostsRequest(size.intValue(), lastPostId);
        CursorResponse<PostResponse> response = postsService.getFriendsPostsCursor(SecurityUtils.getCurrentUserId(), request);
        return BaseResponse.success(response, ResultCode.SUCCESS, "Posts fetched successfully");
    }

    @GetMapping("/feeds/suggested")
    public ResponseEntity<BaseResponse<CursorResponse<PostResponse>>> getSuggestedPosts(@RequestParam(required = false, defaultValue = "4") Long size, @RequestParam(required = false) Long lastPostId) {
        CursorPostsRequest request = new CursorPostsRequest(size.intValue(), lastPostId);
        CursorResponse<PostResponse> response = postsService.getSuggestedPostsCursor(SecurityUtils.getCurrentUserId(), request);
        return BaseResponse.success(response, ResultCode.SUCCESS, "Posts fetched successfully");
    }

    @GetMapping("/search")
    public ResponseEntity<BaseResponse<CommonPage<PostResponse>>> getPosts(
            @ParameterObject PostsSearchRequest searchRequest,
            @RequestParam(required = false, defaultValue = "id") String sortField,
            @RequestParam(required = false, defaultValue = "DESC") Sort.Direction sortDirection,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "4") Integer size
    ) {
        final PaginationRequest pageable = new PaginationRequest(page, size, sortField, sortDirection);
        CommonPage<PostResponse> response = postsService.searchPost(searchRequest, PaginationUtils.getPageable(pageable));
        return BaseResponse.success(response, ResultCode.SUCCESS, "Posts fetched successfully");
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<BaseResponse<CommonPage<PostResponse>>> getUserPosts(
            @PathVariable Long userId,
            @RequestParam(required = false, defaultValue = "id") String sortField,
            @RequestParam(required = false, defaultValue = "DESC") Sort.Direction sortDirection,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "4") Integer size
    ) {
        final PaginationRequest pageable = new PaginationRequest(page, size, sortField, sortDirection);
        CommonPage<PostResponse> response = postsService.getUserPosts(userId, PaginationUtils.getPageable(pageable));
        return BaseResponse.success(response, ResultCode.SUCCESS, "Posts fetched successfully");
    }

    @GetMapping("/{postId}")
    public ResponseEntity<BaseResponse<PostsDetailResponse>> getPostById(@PathVariable Long postId) {
        // Implementation for fetching a post by ID can be added here
        PostsDetailResponse response = postsService.getPostDetail(postId);
        return BaseResponse.success(response, ResultCode.SUCCESS, "Post fetched successfully");
    }

    @PatchMapping(value = "/{postId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BaseResponse<Void>> updatePost(@PathVariable Long postId,
                                                         @ModelAttribute @Valid PostsUpdateRequest request) {
        postsService.updatePost(postId, request);
        return BaseResponse.success(null, ResultCode.SUCCESS, "Post updated successfully");
    }

}
