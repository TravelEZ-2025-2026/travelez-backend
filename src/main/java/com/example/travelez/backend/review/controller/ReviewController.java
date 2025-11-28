package com.example.travelez.backend.review.controller;

import com.example.travelez.backend.common.api.BaseResponse;
import com.example.travelez.backend.common.api.CommonPage;
import com.example.travelez.backend.common.api.ResultCode;
import com.example.travelez.backend.common.api.doc.ApiBaseResponses;
import com.example.travelez.backend.common.dto.PaginationRequest;
import com.example.travelez.backend.common.utils.PaginationUtils;
import com.example.travelez.backend.review.dto.request.ReviewFilterRequest;
import com.example.travelez.backend.review.dto.response.ReviewBaseResponse;
import com.example.travelez.backend.review.service.ReviewService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Review", description = "Review endpoints")
public class ReviewController {

        private final ReviewService reviewService;

        @Operation(summary = "Get list review by poi id", description = "Get list review by poi id")
        @ApiBaseResponses
        @ApiResponse(responseCode = "200", description = "Reviews fetched successfully")
        @GetMapping("/poi/{poiId}/review")
        public ResponseEntity<BaseResponse<CommonPage<ReviewBaseResponse>>> getReviewByPoiId(@PathVariable Long poiId,
                        @RequestParam(required = false) Double rating,
                        @RequestParam(required = false, defaultValue = "id") String sortField,
                        @RequestParam(required = false, defaultValue = "ASC") Sort.Direction sortDirection,
                        @RequestParam(required = false, defaultValue = "0") Integer page,
                        @RequestParam(required = false, defaultValue = "10") Integer size) {
                final PaginationRequest request = new PaginationRequest(page, size, sortField, sortDirection);
                ReviewFilterRequest reviewFilterRequest = ReviewFilterRequest.builder()
                                .poiId(poiId)
                                .rating(rating)
                                .build();
                CommonPage<ReviewBaseResponse> pageResponse = reviewService.getReviewByPoiId(reviewFilterRequest,
                                PaginationUtils.getPageable(request));
                return BaseResponse.success(pageResponse, ResultCode.SUCCESS, "Reviews fetched successfully");
        }

}
