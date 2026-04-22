package com.example.travelez.backend.posts.permission;

import com.example.travelez.backend.posts.model.Posts;

public interface PostsPermissionChecker {
    boolean canUserInteractPost(Long userId, Long postId);

    boolean canUserInteractPost(Long userId, Posts post);

    boolean canUserViewPost(Posts post);
}
