package com.example.travelez.backend.review.service.impl;

import com.example.travelez.backend.common.api.CommonPage;
import com.example.travelez.backend.common.api.ResultCode;
import com.example.travelez.backend.common.exception.ApiException;
import com.example.travelez.backend.poi.model.Poi;
import com.example.travelez.backend.poi.model.enums.PoiStatus;
import com.example.travelez.backend.poi.service.PoiService;
import com.example.travelez.backend.review.dto.request.ReviewFilterRequest;
import com.example.travelez.backend.review.dto.response.ReviewBaseResponse;
import com.example.travelez.backend.review.model.Review;
import com.example.travelez.backend.review.model.enums.ReviewStatus;
import com.example.travelez.backend.review.repository.ReviewRepository;
import com.example.travelez.backend.review.repository.specification.ReviewSpecification;
import com.example.travelez.backend.review.mapper.ReviewMapper;
import com.example.travelez.backend.review.service.ReviewService;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewServiceImpl implements ReviewService {

        private final PoiService poiService;
        private final ReviewRepository reviewRepository;
        private final ReviewMapper reviewMapper;

        @Override
        public CommonPage<ReviewBaseResponse> getReviewByPoiId(ReviewFilterRequest reviewFilterRequest,
                        Pageable pageable) {
                // check poi is active
                poiService.findByIdAndSystemStatus(reviewFilterRequest.getPoiId(), PoiStatus.ACTIVE)
                                .orElseThrow(() -> new ApiException(ResultCode.NOT_FOUND, "Poi not found"));

                List<Specification<Review>> specs = new ArrayList<>();
                specs.add(ReviewSpecification.filterByPoiId(reviewFilterRequest.getPoiId()));
                specs.add(ReviewSpecification.filterByRating(reviewFilterRequest.getRating()));
                specs.add(ReviewSpecification.filterBySystemStatus(ReviewStatus.ACTIVE));

                Page<Review> reviews = reviewRepository.findAll(Specification.allOf(specs), pageable);
                List<ReviewBaseResponse> reviewDetailResponses = reviews.stream()
                                .map(reviewMapper::toReviewBaseResponse)
                                .toList();

                return new CommonPage<>(reviewDetailResponses, reviews.getTotalPages(), reviews.getTotalElements(),
                                pageable.getPageSize(), reviews.getNumber(), reviews.isEmpty());
        }
}
