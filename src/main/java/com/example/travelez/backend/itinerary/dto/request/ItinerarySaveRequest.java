package com.example.travelez.backend.itinerary.dto.request;

import com.example.travelez.backend.itinerary.dto.response.ItineraryResponse;
import lombok.Data;

@Data
public class ItinerarySaveRequest {
    private ItineraryCreationRequest createRequest;
    private ItineraryResponse aiResult;
}
