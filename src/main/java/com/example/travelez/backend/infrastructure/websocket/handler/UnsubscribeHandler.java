package com.example.travelez.backend.infrastructure.websocket.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class UnsubscribeHandler implements StompCommandHandler {

    @Override
    public StompCommand getCommand() {
        return StompCommand.UNSUBSCRIBE;
    }

    @Override
    public void handle(StompHeaderAccessor accessor) {
        String subscriptionId = accessor.getSubscriptionId();
        log.debug("Unsubscribe request: id={}", subscriptionId);
    }
}
