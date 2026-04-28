package com.example.travelez.backend.itinerary.service;

import com.example.travelez.backend.common.api.CommonPage;
import com.example.travelez.backend.itinerary.dto.response.ItinerarySummaryResponse;
import org.springframework.data.domain.Pageable;

public interface ItineraryManagementService {
    void shareItineraryWithUser(Long itineraryId, String username);
    void removeSharedUser(Long itineraryId, String username);
    CommonPage<ItinerarySummaryResponse> getSharedWithMeItineraries(Pageable pageable);
    void exportToGoogleCalendar(Long itineraryId);
}
