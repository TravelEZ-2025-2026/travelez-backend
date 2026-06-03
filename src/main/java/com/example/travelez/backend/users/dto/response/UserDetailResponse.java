package com.example.travelez.backend.users.dto.response;

import com.example.travelez.backend.media.dto.response.MediaBaseResponse;
import com.example.travelez.backend.users.model.enums.GenderType;
import com.example.travelez.backend.users.model.enums.RoleType;
import com.example.travelez.backend.users.model.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDetailResponse {
    private long id;

    private String username;

    private String email;

    private String fullName;

    private GenderType gender;

    private LocalDate dob;

    private UserStatus status;

    private MediaBaseResponse avatar;

    private MediaBaseResponse cover;

    private boolean isFollowedByMe;

    private boolean isFollowingMe;

    private RoleType role;

    private Long followerCount;

    private Long followingCount;
}
