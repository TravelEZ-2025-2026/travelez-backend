package com.example.travelez.backend.users.dto.response;

import com.example.travelez.backend.media.dto.response.MediaBaseResponse;
import com.example.travelez.backend.users.model.enums.AuthProvider;
import com.example.travelez.backend.users.model.enums.GenderType;
import com.example.travelez.backend.users.model.enums.RoleType;
import com.example.travelez.backend.users.model.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class UserAdminDetailResponse {
    private Long id;
    private String username;
    private String email;
    private String fullName;
    private GenderType gender;
    private LocalDateTime dob;
    private UserStatus status;
    private RoleType role;
    private MediaBaseResponse avatar;
    private MediaBaseResponse cover;
    private Long followerCount;
    private Long followingCount;
    private String googleId;
    private AuthProvider authProvider;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
