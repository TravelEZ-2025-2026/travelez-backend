package com.example.travelez.backend.itinerary.repository.specification;

import com.example.travelez.backend.itinerary.model.Itinerary;
import org.springframework.data.jpa.domain.Specification;

public class ItinerarySpecification {
    public static Specification<Itinerary> belongsToUser(Long userId) {
        return (root, query, criteriaBuilder) -> {
            if (userId == null) return null;
            return criteriaBuilder.equal(root.get("traveler").get("id"), userId);
        };
    }
}
