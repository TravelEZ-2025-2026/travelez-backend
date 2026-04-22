package com.example.travelez.backend.chat.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class MessageRecalledPayload {
    private Long messageId;
    private Long conversationId;
}

