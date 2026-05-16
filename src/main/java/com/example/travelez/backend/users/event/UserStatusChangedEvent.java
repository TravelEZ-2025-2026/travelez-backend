package com.example.travelez.backend.users.event;

import com.example.travelez.backend.users.model.enums.ActionType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserStatusChangedEvent {
    private Long userId;
    private ActionType actionType;
    private String reason;
}
