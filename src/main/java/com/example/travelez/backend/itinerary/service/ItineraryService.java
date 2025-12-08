package com.example.travelez.backend.itinerary.service;

import com.example.travelez.backend.itinerary.dto.request.CreateItineraryRequest;
import com.example.travelez.backend.itinerary.dto.request.SaveItineraryRequest;
import com.example.travelez.backend.itinerary.dto.response.ItineraryResponse;

public interface ItineraryService {
    ItineraryResponse generateSmartItinerary(CreateItineraryRequest request);
    Long saveItinerary(SaveItineraryRequest request);
    ItineraryResponse getItineraryDetail(Long itineraryId);
}
