package com.example.travelez.backend.itinerary.dto.response;

import com.example.travelez.backend.itinerary.model.enums.ItineraryStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class ItinerarySummaryResponse {
    private Long id;
    private String title;
    private List<String> destinationCities;
    private List<String> styles;
    private LocalDate startDate;
    private LocalDate endDate;
    private ItineraryStatus status;
    private LocalDateTime createdAt;
    private String ownerUsername;
    private Boolean isPublic;
    private LocalDateTime calendarSyncedAt;
}
