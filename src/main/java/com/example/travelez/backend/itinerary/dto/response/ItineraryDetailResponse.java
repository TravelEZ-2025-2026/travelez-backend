package com.example.travelez.backend.itinerary.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class ItineraryDetailResponse extends ItineraryResponse {
    private Boolean hasKids;
    private Boolean hasPets;
    private String companion;
    private List<String> styles;
    private String specialNotes;
}
