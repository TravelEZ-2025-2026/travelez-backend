package com.example.travelez.backend.social.service;

public interface FollowService {

    void followUser(Long targetUserId);

    void unfollowUser(Long targetUserId);

}
