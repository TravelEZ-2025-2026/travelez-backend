package com.example.travelez.backend.itinerary.service;

import com.example.travelez.backend.common.api.CommonPage;
import com.example.travelez.backend.itinerary.dto.response.ItinerarySummaryResponse;
import com.example.travelez.backend.itinerary.dto.response.SharedUserSearchResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ItineraryManagementService {
    void shareItineraryWithUser(Long itineraryId, String username);
    void removeSharedUser(Long itineraryId, String username);
    CommonPage<ItinerarySummaryResponse> getSharedWithMeItineraries(Pageable pageable);
    void exportToGoogleCalendar(Long itineraryId);
    List<SharedUserSearchResponse> searchSharedUsers(Long itineraryId, String keyword);
    void togglePublicStatus(Long itineraryId, boolean isPublic);
    CommonPage<ItinerarySummaryResponse> searchPublicItineraries(String prompt, Pageable pageable);
    CommonPage<ItinerarySummaryResponse> getUserPublicItineraries(Long userId, Pageable pageable);
    CommonPage<SharedUserSearchResponse> getSharedUsers(Long itineraryId, Pageable pageable);
    CommonPage<ItinerarySummaryResponse> getAllPublicItineraries(Pageable pageable);
}
