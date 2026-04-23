package com.example.travelez.backend.itinerary.controller;

import com.example.travelez.backend.common.api.BaseResponse;
import com.example.travelez.backend.common.api.CommonPage;
import com.example.travelez.backend.common.api.ResultCode;
import com.example.travelez.backend.itinerary.dto.response.ItinerarySummaryResponse;
import com.example.travelez.backend.itinerary.service.ItineraryManagementService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/management/itineraries")
@RequiredArgsConstructor
@Tag(name= "Itinerary management - M2", description = "Itinerary management endpoints")
public class ItineraryManagementController {

    private final ItineraryManagementService itineraryManagementService;

    @PostMapping("/{id}/share")
    public ResponseEntity<BaseResponse<Void>> shareWithUser(
            @PathVariable Long id,
            @RequestParam String username) {
        itineraryManagementService.shareItineraryWithUser(id, username);
        return BaseResponse.success(null, ResultCode.SUCCESS, "Shared successfully with user: " + username);
    }

    @DeleteMapping("/{id}/share/{username}")
    public ResponseEntity<BaseResponse<Void>> removeSharedUser(
            @PathVariable Long id,
            @PathVariable String username) {
        itineraryManagementService.removeSharedUser(id, username);
        return BaseResponse.success(null, ResultCode.SUCCESS, "Revoked share access for user.");
    }

    @GetMapping("/shared-with-me")
    public ResponseEntity<BaseResponse<CommonPage<ItinerarySummaryResponse>>> getSharedWithMeItineraries(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        CommonPage<ItinerarySummaryResponse> result = itineraryManagementService.getSharedWithMeItineraries(pageable);

        return BaseResponse.success(result, ResultCode.SUCCESS);
    }

    @PostMapping("/{id}/export-calendar")
    public ResponseEntity<BaseResponse<Void>> exportToGoogleCalendar(@PathVariable Long id) {
        itineraryManagementService.exportToGoogleCalendar(id);
        return BaseResponse.success(null, ResultCode.SUCCESS, "Itinerary exported to Google Calendar successfully");
    }
}
