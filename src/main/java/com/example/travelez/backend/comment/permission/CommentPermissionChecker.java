package com.example.travelez.backend.comment.permission;

import com.example.travelez.backend.comment.model.Comment;

public interface CommentPermissionChecker {
    boolean canUserInteractComment(Long userId, Long commentId);

    boolean canDeleteComment(Long userId, Long commentId);

    boolean canDeleteComment(Long userId, Comment commentId);
}
