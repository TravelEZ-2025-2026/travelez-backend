package com.example.travelez.backend.posts.service;

import com.example.travelez.backend.common.api.CommonPage;
import com.example.travelez.backend.posts.dto.request.AdminPostsFilterRequest;
import com.example.travelez.backend.posts.dto.request.BanPostRequest;
import com.example.travelez.backend.posts.dto.request.PostStatRequest;
import com.example.travelez.backend.posts.dto.request.UnbanPostRequest;
import com.example.travelez.backend.posts.dto.response.AdminPostsResponse;
import com.example.travelez.backend.posts.dto.response.PostStatResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;

@PreAuthorize("hasRole('ADMIN')")
public interface AdminPostsService {

    PostStatResponse getStatistics(PostStatRequest request);

    CommonPage<AdminPostsResponse> getAllPosts(AdminPostsFilterRequest request, Pageable pageable);

    void banPost(Long postId, BanPostRequest request);

    void unbanPost(Long postId, UnbanPostRequest request);

}
