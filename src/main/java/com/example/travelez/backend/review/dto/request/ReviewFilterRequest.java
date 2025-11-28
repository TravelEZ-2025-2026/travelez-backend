package com.example.travelez.backend.review.dto.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReviewFilterRequest {
    private Long poiId;
    private Double rating;
}
