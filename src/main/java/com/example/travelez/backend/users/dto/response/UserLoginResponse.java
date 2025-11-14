package com.example.travelez.backend.users.dto.response;

import com.example.travelez.backend.users.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserLoginResponse {

    private String token;

    private Long userId;
    private User.RoleType role;
}
