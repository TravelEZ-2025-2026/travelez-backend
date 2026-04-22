package com.example.travelez.backend.chat.model;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConversationMemberId implements Serializable {
    private Long conversationId;
    private Long userId;
}
