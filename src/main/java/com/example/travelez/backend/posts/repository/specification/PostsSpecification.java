package com.example.travelez.backend.posts.repository.specification;

import com.example.travelez.backend.posts.model.Posts;
import com.example.travelez.backend.posts.model.enums.PostStatus;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class PostsSpecification {
    public static Specification<Posts> filterByKeyword(String keyword) {
        return (root, query, criteriaBuilder) -> {
            if (keyword == null) {
                return null;
            }
            String lowerKeyword = keyword.toLowerCase();
            return criteriaBuilder.or(
                    criteriaBuilder.like(root.get("title"), "%" + lowerKeyword + "%"),
                    criteriaBuilder.like(root.get("content"), "%" + lowerKeyword + "%")
            );
        };
    }

    public static Specification<Posts> filterByDateRange(LocalDate fromDate, LocalDate toDate) {
        return (root, query, criteriaBuilder) -> {
            if (fromDate == null && toDate == null) {
                return null;
            }
            if (fromDate == null) {
                return criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), toDate.atTime(LocalTime.MAX));
            }
            if (toDate == null) {
                return criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), fromDate.atStartOfDay());
            }
            return criteriaBuilder.between(root.get("createdAt"), fromDate.atStartOfDay(), toDate.atTime(LocalTime.MAX));
        };
    }

    public static Specification<Posts> filterByStatus(List<PostStatus> statuses) {
        return (root, query, criteriaBuilder) -> {
            if (statuses == null || statuses.isEmpty()) {
                return null;
            }
            return criteriaBuilder.in(root.get("status")).value(statuses);
        };
    }

    public static Specification<Posts> filterByUserId(Long userId) {
        return (root, query, criteriaBuilder) -> {
            return criteriaBuilder.equal(root.get("user").get("id"), userId);
        };
    }

    public static Specification<Posts> filterByTopicTag(String topicTag) {
        return (root, query, criteriaBuilder) -> {
            if (topicTag == null) {
                return null;
            }
            String topicTagLowerCase = topicTag.trim().toLowerCase();
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("topicTag")), "%" + topicTagLowerCase + "%");
        };
    }

    public static Specification<Posts> filterByPoiId(Long poiId) {
        return (root, query, criteriaBuilder) -> {
            if (poiId == null) {
                return null;
            }
            return criteriaBuilder.equal(root.get("poi").get("id"), poiId);
        };
    }
    
}
