package com.example.travelez.backend.chat.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class CursorConversationResponse {
    private LocalDateTime lastMessageAt;
    private Long lastConversationId;
}
