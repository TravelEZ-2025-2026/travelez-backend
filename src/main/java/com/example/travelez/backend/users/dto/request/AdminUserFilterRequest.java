package com.example.travelez.backend.users.dto.request;

import com.example.travelez.backend.users.model.enums.UserStatus;
import lombok.Data;

@Data
public class AdminUserFilterRequest {
    private UserStatus status;
    private String keyword;
}
