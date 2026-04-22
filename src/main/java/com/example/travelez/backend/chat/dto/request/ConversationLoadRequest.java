package com.example.travelez.backend.chat.dto.request;

import lombok.Data;

@Data
public class ConversationLoadRequest {
    private int size = 10;

    private String nextCursor;
}
