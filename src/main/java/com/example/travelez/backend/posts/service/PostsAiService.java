package com.example.travelez.backend.posts.service;

import com.example.travelez.backend.posts.dto.request.PostsSearchRequest;
import com.example.travelez.backend.posts.dto.response.PostsCreatedPayload;
import com.example.travelez.backend.posts.dto.response.PostsUpdatedPayload;

import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PostsAiService {
    public void saveOrUpdatePostVector(PostsCreatedPayload payload);

    public List<Long> searchSimilarPosts(String query, int topK);

    public List<Long> searchWithPagination(PostsSearchRequest request, Pageable pageable);

    public void deletePostVector(Long postId);

    public void updatePostVector(PostsUpdatedPayload payload);
}
