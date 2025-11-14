package com.example.travelez.backend.users.dto.response;

import com.example.travelez.backend.users.model.User.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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

    private LocalDateTime dob;

    private UserStatus status;

    private String avatar;

    private RoleType role;
}
