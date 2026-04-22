package com.example.travelez.backend.infrastructure.websocket.impl;

import com.example.travelez.backend.infrastructure.websocket.SocketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SocketServiceImpl implements SocketService {
    private final SimpMessageSendingOperations messagingSendingOperations;

    @Override
    public void sendToTopic(String destination, Object payload) {
        try {
            messagingSendingOperations.convertAndSend(destination, payload);
            log.info("Sent to topic {}", destination);
        } catch (Exception e) {
            log.error("Failed to send to topic", e);
        }
    }
}
