package com.example.travelez.backend.chat.dto.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MessageLoadRequest {
    private Long conversationId;
    private int size = 10;
    private Long lastMessageId;
}
