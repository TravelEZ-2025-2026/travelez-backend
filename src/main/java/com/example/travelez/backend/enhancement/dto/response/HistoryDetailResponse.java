package com.example.travelez.backend.enhancement.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class HistoryDetailResponse {
    private Long id;
    private String fileName;
    private String providerPrompt;
    private ItineraryEnhancementResponse analysisResult;
    private LocalDateTime createdAt;
}
