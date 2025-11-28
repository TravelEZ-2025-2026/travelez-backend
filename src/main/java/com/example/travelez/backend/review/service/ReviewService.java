package com.example.travelez.backend.review.service;

import com.example.travelez.backend.review.dto.response.ReviewBaseResponse;
import org.springframework.data.domain.Pageable;

import com.example.travelez.backend.common.api.CommonPage;
import com.example.travelez.backend.review.dto.request.ReviewFilterRequest;

public interface ReviewService {
    public CommonPage<ReviewBaseResponse> getReviewByPoiId(ReviewFilterRequest reviewFilterRequest,
                                                           Pageable pageable);
}
