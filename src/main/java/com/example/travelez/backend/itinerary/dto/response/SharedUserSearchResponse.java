package com.example.travelez.backend.itinerary.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SharedUserSearchResponse {
    private Long userId;
    private String username;
    private String avatarUrl;
    private LocalDateTime sharedAt;
}
