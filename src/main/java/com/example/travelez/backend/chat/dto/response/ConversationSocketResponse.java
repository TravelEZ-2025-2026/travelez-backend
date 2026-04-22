package com.example.travelez.backend.chat.dto.response;

import com.example.travelez.backend.chat.model.enums.ConversationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationSocketResponse {
    private String avatar;
    private String groupName;
    private ConversationType type;
    private boolean isGroup;
}

