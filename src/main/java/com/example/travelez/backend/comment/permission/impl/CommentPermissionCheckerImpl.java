package com.example.travelez.backend.comment.permission.impl;

import com.example.travelez.backend.comment.model.Comment;
import com.example.travelez.backend.comment.permission.CommentPermissionChecker;
import com.example.travelez.backend.comment.repository.CommentRepository;
import com.example.travelez.backend.common.api.ResultCode;
import com.example.travelez.backend.common.exception.ApiException;
import com.example.travelez.backend.posts.model.Posts;
import com.example.travelez.backend.posts.model.enums.PostStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CommentPermissionCheckerImpl implements CommentPermissionChecker {
    private final CommentRepository commentRepository;

    @Override
    public boolean canUserInteractComment(Long userId, Long commentId) {
        return commentRepository.existsById(commentId);
    }

    @Override
    public boolean canDeleteComment(Long userId, Long commentId) {
        Comment comment = commentRepository.findByIdWithPost(commentId)
                .orElseThrow(() -> new ApiException(ResultCode.NOT_FOUND, "Comment not found"));
        return canDeleteComment(userId, comment);
    }

    @Override
    public boolean canDeleteComment(Long userId, Comment comment) {
        if (userId == null) {
            return false;
        }
        Posts posts = comment.getPost();
        PostStatus status = posts.getStatus();
        if (status == PostStatus.BANNED) {
            return false;
        }
        boolean isPostOwner = posts.getUser().getId() == userId;
        if (status == PostStatus.ARCHIVED) {
            return isPostOwner;
        }
        boolean isCommentOwner = comment.getUser().getId() == userId;
        return isPostOwner || isCommentOwner;
    }
}
