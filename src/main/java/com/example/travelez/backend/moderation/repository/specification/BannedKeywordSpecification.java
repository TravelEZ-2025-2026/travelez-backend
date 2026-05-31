package com.example.travelez.backend.moderation.repository.specification;

import com.example.travelez.backend.moderation.model.BannedKeyword;
import com.example.travelez.backend.moderation.model.enums.KeywordSeverity;
import com.example.travelez.backend.moderation.model.enums.ViolationType;
import org.springframework.data.jpa.domain.Specification;

public class BannedKeywordSpecification {

    public static Specification<BannedKeyword> filterByViolationType(ViolationType violationType) {
        return (root, query, cb) ->
                violationType == null ? null : cb.equal(root.get("violationType"), violationType);
    }

    public static Specification<BannedKeyword> filterBySeverity(KeywordSeverity severity) {
        return (root, query, cb) ->
                severity == null ? null : cb.equal(root.get("severity"), severity);
    }

    public static Specification<BannedKeyword> filterByIsActive(Boolean isActive) {
        return (root, query, cb) ->
                isActive == null ? null : cb.equal(root.get("isActive"), isActive);
    }

    public static Specification<BannedKeyword> filterByKeyword(String keyword) {
        return (root, query, cb) ->
                keyword == null || keyword.isBlank()
                        ? null
                        : cb.like(cb.lower(root.get("keyword")), "%" + keyword.toLowerCase() + "%");
    }
}
