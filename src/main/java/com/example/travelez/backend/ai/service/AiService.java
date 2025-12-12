package com.example.travelez.backend.ai.service;

import com.example.travelez.backend.itinerary.dto.request.ItineraryCreationRequest;
import com.example.travelez.backend.itinerary.dto.response.ItineraryResponse;

public interface AiService {
    ItineraryResponse generateItinerary(ItineraryCreationRequest request, String poiContextJson);
}
