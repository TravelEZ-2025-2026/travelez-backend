package com.example.travelez.backend.infrastructure.websocket.handler;

import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;

public interface StompCommandHandler {
    StompCommand getCommand();

    void handle(StompHeaderAccessor accessor);
}
