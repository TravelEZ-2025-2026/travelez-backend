package com.example.travelez.backend.itinerary.service;

import com.example.travelez.backend.itinerary.dto.request.ItineraryCreationRequest;
import com.example.travelez.backend.itinerary.dto.request.ItinerarySaveRequest;
import com.example.travelez.backend.itinerary.dto.response.ItineraryDetailResponse;
import com.example.travelez.backend.itinerary.dto.response.ItineraryResponse;

public interface ItineraryService {
    ItineraryResponse generateSmartItinerary(ItineraryCreationRequest request);
    Long saveItinerary(ItinerarySaveRequest request);
    ItineraryDetailResponse getItineraryDetail(Long itineraryId);
}
