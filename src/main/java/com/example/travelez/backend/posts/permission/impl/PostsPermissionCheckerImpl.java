package com.example.travelez.backend.posts.permission.impl;

import com.example.travelez.backend.common.api.ResultCode;
import com.example.travelez.backend.common.exception.ApiException;
import com.example.travelez.backend.common.utils.SecurityUtils;
import com.example.travelez.backend.posts.model.Posts;
import com.example.travelez.backend.posts.model.enums.PostStatus;
import com.example.travelez.backend.posts.permission.PostsPermissionChecker;
import com.example.travelez.backend.posts.repository.PostsRepository;
import com.example.travelez.backend.security.component.UserPrinciple;
import com.example.travelez.backend.users.model.User;
import com.example.travelez.backend.users.model.enums.RoleType;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@RequiredArgsConstructor
@Slf4j
public class PostsPermissionCheckerImpl implements PostsPermissionChecker {

    private final PostsRepository postsRepository;

    @Override
    public boolean canUserInteractPost(Long userId, Long postId) {
        Posts post = postsRepository.findById(postId)
                .orElseThrow(() -> new ApiException(ResultCode.NOT_FOUND, "Post not found"));
        return canUserInteractPost(userId, post);
    }

    @Override
    public boolean canUserInteractPost(Long userId, Posts post) {
        PostStatus status = post.getStatus();
        if (status == PostStatus.BANNED) {
            return false;
        }
        boolean isOwner = Objects.equals(post.getUser().getId(), userId);
        if (isOwner) return true;
        return status == PostStatus.PUBLISHED;
    }

    @Override
    public boolean canUserViewPost(Posts post) {
        UserPrinciple user = SecurityUtils.getUserPrinciple();
        boolean isAdmin = user != null && user.getRole().equals(RoleType.ADMIN.name());
        if (isAdmin) return true;
        boolean isOwner = user != null && post.getUser().getId() == user.getUserId();
        if (post.getStatus() == PostStatus.ARCHIVED || post.getStatus() == PostStatus.BANNED) {
            return isOwner;
        }
        return true;
    }
}
