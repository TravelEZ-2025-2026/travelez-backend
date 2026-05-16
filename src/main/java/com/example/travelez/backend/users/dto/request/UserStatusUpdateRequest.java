package com.example.travelez.backend.users.dto.request;

import com.example.travelez.backend.users.model.enums.ActionType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserStatusUpdateRequest {
    
    @NotNull(message = "Action is required")
    private ActionType action;
    
    private String reason;
}
