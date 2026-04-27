package com.example.travelez.backend.review.service;

import com.example.travelez.backend.common.api.CommonPage;
import com.example.travelez.backend.review.dto.request.ReviewCreateRequest;
import com.example.travelez.backend.review.dto.request.ReviewFilterRequest;
import com.example.travelez.backend.review.dto.response.ReviewBaseResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ReviewService {
    public CommonPage<ReviewBaseResponse> getReviewByPoiId(ReviewFilterRequest reviewFilterRequest,
                                                           Pageable pageable);

    public ReviewBaseResponse createReview(Long poiId, ReviewCreateRequest request, List<MultipartFile> files);

    public void deleteReview(Long reviewId);

    CommonPage<ReviewBaseResponse> getReviewByUserId(Long userId, Pageable pageable);
}
