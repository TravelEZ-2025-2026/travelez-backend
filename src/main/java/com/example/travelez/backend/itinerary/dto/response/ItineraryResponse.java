package com.example.travelez.backend.itinerary.dto.response;

import com.example.travelez.backend.itinerary.dto.response.utils.DayPlan;
import com.example.travelez.backend.itinerary.dto.response.utils.EstimatedBudget;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ItineraryResponse {
    private String tempId;
    private String tripTitle;
    private List<String> destinationCities;
    private String reasoningSummary;
    private EstimatedBudget estimatedBudget;
    private List<DayPlan> days;
}