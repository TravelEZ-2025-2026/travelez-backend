package com.example.travelez.backend.itinerary.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ItineraryDetailResponse extends ItineraryResponse {
    private Long id;
    private Long userId;
    private Boolean isPublic;

    private Boolean hasKids;
    private Boolean hasPets;
    private String companion;
    private List<String> styles;
    private String specialNotes;
    private LocalDateTime calendarSyncedAt;
}
