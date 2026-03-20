package com.example.travelez.backend.comment.controller;

import com.example.travelez.backend.comment.dto.request.CommentCreateRequest;
import com.example.travelez.backend.comment.dto.request.CommentUpdateRequest;
import com.example.travelez.backend.comment.dto.response.CommentBaseResponse;
import com.example.travelez.backend.comment.service.CommentService;
import com.example.travelez.backend.common.api.BaseResponse;
import com.example.travelez.backend.common.api.CommonPage;
import com.example.travelez.backend.common.api.ResultCode;
import com.example.travelez.backend.common.dto.PaginationRequest;
import com.example.travelez.backend.common.utils.PaginationUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Comment", description = "Comments endpoints")
public class CommentController {
    private final CommentService commentService;

    @PostMapping(value = "/posts/{postId}/comments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BaseResponse<Void>> createComment(@PathVariable Long postId, @ModelAttribute @Valid CommentCreateRequest request) {
        commentService.createComment(postId, request);
        return BaseResponse.success(null, ResultCode.CREATED, "Comment created successfully");
    }

    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<BaseResponse<CommonPage<CommentBaseResponse>>> getCommentsByPostId(@PathVariable Long postId,
                                                                                             @RequestParam(required = false, defaultValue = "id") String sortField,
                                                                                             @RequestParam(required = false, defaultValue = "ASC") Sort.Direction sortDirection,
                                                                                             @RequestParam(required = false, defaultValue = "0") Integer page,
                                                                                             @RequestParam(required = false, defaultValue = "10") Integer size) {
        final PaginationRequest request = new PaginationRequest(page, size, sortField, sortDirection);
//        CommentFilterRequest filterRequest = CommentFilterRequest.builder()
//                .postId(postId)
//                .build();
        CommonPage<CommentBaseResponse> response = commentService.getCommentsByPostId(postId, PaginationUtils.getPageable(request));
        return BaseResponse.success(response, ResultCode.SUCCESS, "Comments fetched successfully");
    }

    @GetMapping("/comments/{commentId}/replies")
    public ResponseEntity<BaseResponse<CommonPage<CommentBaseResponse>>> getRepliesByCommentId(@PathVariable Long commentId,
                                                                                               @RequestParam(required = false, defaultValue = "id") String sortField,
                                                                                               @RequestParam(required = false, defaultValue = "ASC") Sort.Direction sortDirection,
                                                                                               @RequestParam(required = false, defaultValue = "0") Integer page,
                                                                                               @RequestParam(required = false, defaultValue = "10") Integer size) {
        final PaginationRequest request = new PaginationRequest(page, size, sortField, sortDirection);
        CommonPage<CommentBaseResponse> response = commentService.getRepliesByCommentId(commentId, PaginationUtils.getPageable(request));
        return BaseResponse.success(response, ResultCode.SUCCESS, "Replies fetched successfully");
    }

    @PatchMapping(value = "/comments/{commentId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BaseResponse<Void>> updateComment(@PathVariable Long commentId, @ModelAttribute @Valid CommentUpdateRequest request) {
        commentService.updateComment(commentId, request);
        return BaseResponse.success(null, ResultCode.SUCCESS, "Comment updated successfully");
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<BaseResponse<Void>> deleteComment(@PathVariable Long commentId) {
        commentService.deleteComment(commentId);
        return BaseResponse.success(null, ResultCode.SUCCESS, "Comment deleted successfully");
    }

}
