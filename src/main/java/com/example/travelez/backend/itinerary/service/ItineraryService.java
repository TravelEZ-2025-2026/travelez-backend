package com.example.travelez.backend.itinerary.service;

import com.example.travelez.backend.common.api.CommonPage;
import com.example.travelez.backend.itinerary.dto.request.ItineraryCreationRequest;
import com.example.travelez.backend.itinerary.dto.request.ItinerarySaveRequest;
import com.example.travelez.backend.itinerary.dto.response.ItineraryDetailResponse;
import com.example.travelez.backend.itinerary.dto.response.ItineraryResponse;
import com.example.travelez.backend.itinerary.dto.response.ItinerarySummaryResponse;
import org.springframework.data.domain.Pageable;

public interface ItineraryService {
    ItineraryResponse generateSmartItinerary(ItineraryCreationRequest request);
    Long saveItinerary(ItinerarySaveRequest request);
    ItineraryDetailResponse getItineraryDetail(Long itineraryId);
    CommonPage<ItinerarySummaryResponse> getItineraryList(Pageable pageable);
    void deleteItinerary(Long itineraryId);
}
