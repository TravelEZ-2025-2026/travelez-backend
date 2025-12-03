package com.example.travelez.backend.poi.repository.specification;

import com.example.travelez.backend.poi.model.Place;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public class PlaceSpecification {
    public static Specification<Place> filterByName(String name) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(name)) {
                return null;
            }
            String nameLowerCase = name.trim().toLowerCase();
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), "%" + nameLowerCase + "%");
        };
    }

    public static Specification<Place> filterByCountry(String country) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(country)) {
                return null;
            }
            String countryLowerCase = country.trim().toLowerCase();
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("country")), "%" + countryLowerCase + "%");
        };
    }

    public static Specification<Place> filterByCountryCode(String countryCode) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(countryCode)) {
                return null;
            }
            return criteriaBuilder.equal(root.get("countryCode"), countryCode);
        };
    }

}
