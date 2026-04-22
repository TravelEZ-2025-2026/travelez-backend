package com.example.travelez.backend.social.service;

import java.util.List;
import java.util.Set;

import com.example.travelez.backend.social.dto.internal.RelationshipStatus;

public interface FollowService {

    void followUser(Long targetUserId);

    void unfollowUser(Long targetUserId);

    RelationshipStatus getRelationShip(Long followerId, Long followingId);

    Set<Long> getUserIdsFollowedByMe(Long currentUserId, List<Long> targetUserIds);
}
