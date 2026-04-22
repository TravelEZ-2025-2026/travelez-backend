package com.example.travelez.backend.social.controller;

import com.example.travelez.backend.common.api.BaseResponse;
import com.example.travelez.backend.common.api.ResultCode;
import com.example.travelez.backend.social.service.FollowService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Social", description = "Social endpoints")
public class FollowController {
    private final FollowService followService;

    @PostMapping("/{targetUserId}/follow")
    public ResponseEntity<BaseResponse<Void>> followUser(@PathVariable Long targetUserId) {
        followService.followUser(targetUserId);
        return BaseResponse.success(null, ResultCode.SUCCESS, "User followed successfully");
    }

    @DeleteMapping("/{targetUserId}/unfollow")
    public ResponseEntity<BaseResponse<Void>> unfollowUser(@PathVariable Long targetUserId) {
        followService.unfollowUser(targetUserId);
        return BaseResponse.success(null, ResultCode.SUCCESS, "User unfollowed successfully");
    }
}
