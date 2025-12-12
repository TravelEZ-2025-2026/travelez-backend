package com.example.travelez.backend.ai.service;

import com.example.travelez.backend.itinerary.dto.request.CreateItineraryRequest;
import com.example.travelez.backend.itinerary.dto.response.ItineraryResponse;

public interface TravelEzAiService {
    ItineraryResponse generateItinerary(CreateItineraryRequest request, String poiContextJson);
}
