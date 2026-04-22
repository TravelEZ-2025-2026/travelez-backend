package com.example.travelez.backend.social.repository;

import com.example.travelez.backend.social.model.Follow;
import com.example.travelez.backend.social.model.FollowId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface FollowRepository extends JpaRepository<Follow, FollowId> {
    @Query("SELECT f FROM Follow f WHERE (f.follower.id = :currentUserId AND f.following.id = :targetUserId) OR (f.follower.id = :targetUserId AND f.following.id = :currentUserId)")
    List<Follow> findRelationshipBetweenUsers(@Param("currentUserId") Long currentUserId, @Param("targetUserId") Long targetUserId);

    @Query("SELECT f.following.id FROM Follow f WHERE f.follower.id = :userId AND f.following.id IN :targetUserIds")
    Set<Long> findUserIdsFollowedByMe(@Param("userId") Long userId, @Param("targetUserIds") List<Long> targetUserIds);

}
