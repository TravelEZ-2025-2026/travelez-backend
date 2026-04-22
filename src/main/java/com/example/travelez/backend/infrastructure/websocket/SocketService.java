package com.example.travelez.backend.infrastructure.websocket;

public interface SocketService {
    void sendToTopic(String destination, Object payload);
}
