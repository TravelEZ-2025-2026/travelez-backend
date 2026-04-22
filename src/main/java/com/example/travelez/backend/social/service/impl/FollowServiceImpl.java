package com.example.travelez.backend.social.service.impl;

import com.example.travelez.backend.common.api.ResultCode;
import com.example.travelez.backend.common.exception.Asserts;
import com.example.travelez.backend.common.utils.SecurityUtils;
import com.example.travelez.backend.social.dto.internal.RelationshipStatus;
import com.example.travelez.backend.social.model.Follow;
import com.example.travelez.backend.social.model.FollowId;
import com.example.travelez.backend.social.repository.FollowRepository;
import com.example.travelez.backend.social.service.FollowService;
import com.example.travelez.backend.users.model.User;
import com.example.travelez.backend.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class FollowServiceImpl implements FollowService {

    private final TransactionTemplate transactionTemplate;
    private final FollowRepository followRepository;
    private final UserRepository userRepository;

    @Override
    public void followUser(Long targetUserId) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId.equals(targetUserId)) {
            Asserts.fail(ResultCode.BAD_REQUEST, "You cannot follow yourself");
        }
        transactionTemplate.execute(status -> {
            FollowId id = new FollowId(currentUserId, targetUserId);
            if (followRepository.existsById(id)) {
                return null;
            }
            User follower = userRepository.getReferenceById(currentUserId);
            User following = userRepository.getReferenceById(targetUserId);

            Follow follow = new Follow();
            follow.setId(id);
            follow.setFollower(follower);
            follow.setFollowing(following);
            followRepository.save(follow);

            userRepository.incrementFollowingCount(currentUserId);
            userRepository.incrementFollowerCount(targetUserId);

            return null;
        });
    }

    @Override
    public void unfollowUser(Long targetUserId) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId.equals(targetUserId)) return;
        transactionTemplate.execute(status -> {
            FollowId id = new FollowId(currentUserId, targetUserId);
            if (!followRepository.existsById(id)) {
                return null;
            }

            followRepository.deleteById(id);

            userRepository.decrementFollowingCount(currentUserId);
            userRepository.decrementFollowerCount(targetUserId);

            return null;
        });
    }

    @Override
    public RelationshipStatus getRelationShip(Long currentUserId, Long targetUserId) {
        if (currentUserId == null || currentUserId.equals(targetUserId)) {
            return RelationshipStatus.none();
        }
        List<Follow> relationships = followRepository.findRelationshipBetweenUsers(currentUserId, targetUserId);
        boolean isFollowed = relationships.stream().anyMatch(follow -> currentUserId.equals(follow.getFollower().getId())
                && targetUserId.equals(follow.getFollowing().getId()));
        boolean isFollowing = relationships.stream().anyMatch(follow -> targetUserId.equals(follow.getFollower().getId())
                && currentUserId.equals(follow.getFollowing().getId()));
        return new RelationshipStatus(isFollowed, isFollowing);
    }

    @Override
    public Set<Long> getUserIdsFollowedByMe(Long currentUserId, List<Long> targetUserIds) {
        if (currentUserId == null || targetUserIds == null || targetUserIds.isEmpty()) return Set.of();
        return followRepository.findUserIdsFollowedByMe(currentUserId, targetUserIds);
    }
}
