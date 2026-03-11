package com.example.travelez.backend.itinerary.dto.response;

import com.example.travelez.backend.itinerary.dto.response.utils.DayPlan;
import lombok.Data;

import java.util.List;

@Data
public class ItineraryResponse {
    private String tempId;
    private String tripTitle;
    private List<String> destinationCities;
    private String reasoningSummary;
    private List<DayPlan> days;
}
