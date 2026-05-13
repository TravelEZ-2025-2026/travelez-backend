package com.example.travelez.backend.enhancement.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ItineraryEnhancementHistorySummary {
    private Long id;
    private String originalFileName;
    private String providerPrompt;
    private LocalDateTime createdAt;
}
