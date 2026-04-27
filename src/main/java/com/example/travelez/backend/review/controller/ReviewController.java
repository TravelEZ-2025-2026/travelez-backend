package com.example.travelez.backend.review.controller;

import com.example.travelez.backend.common.api.BaseResponse;
import com.example.travelez.backend.common.api.CommonPage;
import com.example.travelez.backend.common.api.ResultCode;
import com.example.travelez.backend.common.api.doc.ApiBaseResponses;
import com.example.travelez.backend.common.dto.PaginationRequest;
import com.example.travelez.backend.common.utils.PaginationUtils;
import com.example.travelez.backend.review.dto.request.ReviewCreateRequest;
import com.example.travelez.backend.review.dto.request.ReviewFilterRequest;
import com.example.travelez.backend.review.dto.response.ReviewBaseResponse;
import com.example.travelez.backend.review.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Review", description = "Review endpoints")
public class ReviewController {

    private final ReviewService reviewService;

    @Operation(summary = "Get list review by poi id", description = "Get list review by poi id")
    @ApiBaseResponses
    @ApiResponse(responseCode = "200", description = "Reviews fetched successfully")
    @GetMapping("/pois/{poiId}/reviews")
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

    @PreAuthorize("hasRole('TRAVELER')")
    @Operation(summary = "Create review", description = "Create review")
    @ApiBaseResponses
    @ApiResponse(responseCode = "200", description = "Review created successfully")
    @PostMapping(value = "/pois/{poiId}/reviews", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BaseResponse<ReviewBaseResponse>> createReview(@PathVariable Long poiId, @ModelAttribute @Valid ReviewCreateRequest request) {
        ReviewBaseResponse reviewBaseResponse = reviewService.createReview(poiId, request, request.getFiles());
        return BaseResponse.success(reviewBaseResponse, ResultCode.SUCCESS, "Review created successfully");
    }

    @PreAuthorize("hasAnyRole('TRAVELER', 'ADMIN')")
    @Operation(summary = "Delete review", description = "Delete review")
    @ApiBaseResponses
    @ApiResponse(responseCode = "200", description = "Review deleted successfully")
    @DeleteMapping("/reviews/{reviewId}")
    public ResponseEntity<BaseResponse<Void>> deleteReview(@PathVariable Long reviewId) {
        reviewService.deleteReview(reviewId);
        return BaseResponse.success(null, ResultCode.SUCCESS, "Review deleted successfully");
    }

    // get review by user id
    @GetMapping("/users/{userId}/reviews")
    public ResponseEntity<BaseResponse<CommonPage<ReviewBaseResponse>>> getReviewByUserId(@PathVariable Long userId,
                                                                                         @RequestParam(required = false, defaultValue = "id") String sortField,
                                                                                         @RequestParam(required = false, defaultValue = "DESC") Sort.Direction sortDirection,
                                                                                         @RequestParam(required = false, defaultValue = "0") Integer page,
                                                                                         @RequestParam(required = false, defaultValue = "10") Integer size) {
        final PaginationRequest request = new PaginationRequest(page, size, sortField, sortDirection);
        CommonPage<ReviewBaseResponse> response = reviewService.getReviewByUserId(userId, PaginationUtils.getPageable(request));
        return BaseResponse.success(response, ResultCode.SUCCESS, "Reviews fetched successfully");
    }

}
