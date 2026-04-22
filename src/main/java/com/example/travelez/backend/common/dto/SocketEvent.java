package com.example.travelez.backend.common.dto;

import com.example.travelez.backend.common.model.enums.SocketEventType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SocketEvent<T> {
    private SocketEventType type;
    private T payload;
}
