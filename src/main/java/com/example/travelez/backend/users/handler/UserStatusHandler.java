package com.example.travelez.backend.users.handler;

import com.example.travelez.backend.users.model.User;

public interface UserStatusHandler {
    
    void handle(User user, String reason);
    
    boolean canHandle(User user);
}
