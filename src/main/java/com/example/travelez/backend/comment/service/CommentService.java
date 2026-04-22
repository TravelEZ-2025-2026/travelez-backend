package com.example.travelez.backend.comment.service;

import com.example.travelez.backend.comment.dto.request.CommentCreateRequest;
import com.example.travelez.backend.comment.dto.request.CommentUpdateRequest;
import com.example.travelez.backend.comment.dto.response.CommentBaseResponse;
import com.example.travelez.backend.common.api.CommonPage;
import org.springframework.data.domain.Pageable;

public interface CommentService {

    void createComment(Long postId, CommentCreateRequest request);

    void updateComment(Long commentId, CommentUpdateRequest request);

    void deleteComment(Long commentId);

    CommonPage<CommentBaseResponse> getCommentsByPostId(Long postId, Pageable pageable);

    CommonPage<CommentBaseResponse> getRepliesByCommentId(Long commentId, Pageable pageable);
}
