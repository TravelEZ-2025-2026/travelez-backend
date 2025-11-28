package com.example.travelez.backend.review.repository.specification;

import org.springframework.data.jpa.domain.Specification;

import com.example.travelez.backend.review.model.Review;
import com.example.travelez.backend.review.model.enums.ReviewStatus;

public class ReviewSpecification {
    public static Specification<Review> filterByPoiId(Long poiId) {
        return (root, query, criteriaBuilder) -> {
            if (poiId == null) {
                return null;
            }
            return criteriaBuilder.equal(root.get("poi").get("id"), poiId);
        };
    }

    public static Specification<Review> filterByRating(Double rating) {
        return (root, query, criteriaBuilder) -> {
            if (rating == null) {
                return null;
            }
            return criteriaBuilder.greaterThanOrEqualTo(root.get("rating"), rating);
        };
    }

    public static Specification<Review> filterBySystemStatus(ReviewStatus status) {
        return (root, query, criteriaBuilder) -> {
            if (status == null) {
                return null;
            }
            return criteriaBuilder.equal(root.get("status"), status);
        };
    }
}
