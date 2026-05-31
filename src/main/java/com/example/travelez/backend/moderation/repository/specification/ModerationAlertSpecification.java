package com.example.travelez.backend.moderation.repository.specification;

import com.example.travelez.backend.moderation.model.ModerationAlert;
import com.example.travelez.backend.moderation.model.enums.AlertStatus;
import com.example.travelez.backend.moderation.model.enums.ModerationTargetType;
import com.example.travelez.backend.moderation.model.enums.ViolationType;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public class ModerationAlertSpecification {

    public static Specification<ModerationAlert> filterByStatus(AlertStatus status) {
        return (root, query, cb) ->
                status == null ? null : cb.equal(root.get("status"), status);
    }

    public static Specification<ModerationAlert> filterByViolationType(ViolationType violationType) {
        return (root, query, cb) ->
                violationType == null ? null : cb.equal(root.get("violationType"), violationType);
    }

    public static Specification<ModerationAlert> filterByTargetType(ModerationTargetType targetType) {
        return (root, query, cb) ->
                targetType == null ? null : cb.equal(root.get("targetType"), targetType);
    }

    public static Specification<ModerationAlert> filterByCreatedAfter(LocalDateTime fromDate) {
        return (root, query, cb) ->
                fromDate == null ? null : cb.greaterThanOrEqualTo(root.get("createdAt"), fromDate);
    }

    public static Specification<ModerationAlert> filterByCreatedBefore(LocalDateTime toDate) {
        return (root, query, cb) ->
                toDate == null ? null : cb.lessThanOrEqualTo(root.get("createdAt"), toDate);
    }
}
