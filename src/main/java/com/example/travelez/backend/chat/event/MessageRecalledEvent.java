package com.example.travelez.backend.chat.event;

import com.example.travelez.backend.chat.dto.response.MessageRecalledPayload;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MessageRecalledEvent {
    private MessageRecalledPayload payload;
}
