package com.example.travelez.backend.users.repository.specification;

import com.example.travelez.backend.users.model.User;
import com.example.travelez.backend.users.model.enums.RoleType;
import com.example.travelez.backend.users.model.enums.UserStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class UserSpecification {

    public static Specification<User> hasRole(RoleType role) {
        return (root, query, cb) -> cb.equal(root.get("role"), role);
    }

    public static Specification<User> hasStatus(UserStatus status) {
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<User> searchByKeyword(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.trim().isEmpty()) {
                return cb.conjunction();
            }
            String pattern = "%" + keyword.toLowerCase() + "%";
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.like(cb.lower(root.get("username")), pattern));
            predicates.add(cb.like(cb.lower(root.get("email")), pattern));
            predicates.add(cb.like(cb.lower(root.get("fullName")), pattern));
            return cb.or(predicates.toArray(new Predicate[0]));
        };
    }

    // Legacy methods for backward compatibility
    public static Specification<User> filterByKeyword(String keyword) {
        return searchByKeyword(keyword);
    }

    public static Specification<User> filterByStatus(List<UserStatus> statuses) {
        return (root, query, cb) -> root.get("status").in(statuses);
    }

    public static Specification<User> filterByRole(List<RoleType> roles) {
        return (root, query, cb) -> root.get("role").in(roles);
    }
}
