package com.example.travelez.backend.itinerary.dto.request;

import com.example.travelez.backend.itinerary.dto.response.ItineraryResponse;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class SaveItineraryRequest {
    private String destinationCity;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal budget;
    private List<String> styles;
    private ItineraryResponse aiResult;
}
