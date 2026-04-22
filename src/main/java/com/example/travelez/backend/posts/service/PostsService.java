package com.example.travelez.backend.posts.service;

import com.example.travelez.backend.common.api.CommonPage;
import com.example.travelez.backend.common.api.CursorResponse;
import com.example.travelez.backend.posts.dto.request.CursorPostsRequest;
import com.example.travelez.backend.posts.dto.request.PostsCreateRequest;
import com.example.travelez.backend.posts.dto.request.PostsSearchRequest;
import com.example.travelez.backend.posts.dto.request.PostsUpdateRequest;
import com.example.travelez.backend.posts.dto.response.PostResponse;
import com.example.travelez.backend.posts.dto.response.PostsDetailResponse;
import org.springframework.data.domain.Pageable;

public interface PostsService {
    void createPost(PostsCreateRequest request);

    void updatePost(Long postId, PostsUpdateRequest request);

    void deletePost(Long postId);

    PostsDetailResponse getPostDetail(Long postId);

    CursorResponse<PostResponse> getAllPosts(CursorPostsRequest request);

    CommonPage<PostResponse> searchPosts(PostsSearchRequest searchRequest, Pageable pageable);

    CommonPage<PostResponse> getUserPosts(Long userId, Pageable pageable);
}
