package com.example.travelez.backend.users.handler.impl;

import com.example.travelez.backend.users.handler.UserStatusHandler;
import com.example.travelez.backend.users.model.User;
import com.example.travelez.backend.users.model.enums.UserStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class UnbanUserHandler implements UserStatusHandler {
    
    @Override
    public void handle(User user, String reason) {
        log.info("Unbanning user: {} with reason: {}", user.getId(), reason);
        user.setStatus(UserStatus.ACTIVE);
    }
    
    @Override
    public boolean canHandle(User user) {
        return user.getStatus() == UserStatus.BANNED;
    }
}
