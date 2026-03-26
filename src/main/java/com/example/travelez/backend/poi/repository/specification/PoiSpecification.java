package com.example.travelez.backend.poi.repository.specification;

import com.example.travelez.backend.poi.model.Poi;
import com.example.travelez.backend.poi.model.enums.PlaceStatus;
import com.example.travelez.backend.poi.model.enums.PoiStatus;
import com.example.travelez.backend.poi.model.enums.PoiType;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public class PoiSpecification {
    public static Specification<Poi> filterByName(String name) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(name)) {
                return null;
            }
            String nameLowerCase = name.trim().toLowerCase();
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), "%" + nameLowerCase + "%");
        };
    }

    public static Specification<Poi> filterByPoiType(PoiType poiType) {
        return (root, query, criteriaBuilder) -> {
            if (poiType == null) {
                return null;
            }
            return criteriaBuilder.equal(root.get("poiType"), poiType);
        };
    }

    public static Specification<Poi> filterByPlaceStatus(PlaceStatus placeStatus) {
        return (root, query, criteriaBuilder) -> {
            if (placeStatus == null) {
                return null;
            }
            return criteriaBuilder.equal(root.get("status"), placeStatus);
        };
    }

    public static Specification<Poi> filterByRating(Double rating) {
        return (root, query, criteriaBuilder) -> {
            if (rating == null) {
                return null;
            }
            return criteriaBuilder.greaterThanOrEqualTo(root.get("rating"), rating);
        };
    }

    public static Specification<Poi> filterByPlaceId(Long placeId) {
        return (root, query, criteriaBuilder) -> {
            if (placeId == null) {
                return null;
            }
            return criteriaBuilder.equal(root.get("place").get("id"), placeId);
        };
    }

    public static Specification<Poi> filterByWardId(Long wardId) {
        return (root, query, criteriaBuilder) -> {
            if (wardId == null) {
                return null;
            }
            return criteriaBuilder.equal(root.get("ward").get("id"), wardId);
        };
    }

    public static Specification<Poi> filterBySystemStatus(PoiStatus systemStatus) {
        return (root, query, criteriaBuilder) -> {
            if (systemStatus == null) {
                return null;
            }
            return criteriaBuilder.equal(root.get("systemStatus"), systemStatus);
        };
    }

}
