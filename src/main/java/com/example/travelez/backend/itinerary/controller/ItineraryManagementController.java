package com.example.travelez.backend.itinerary.controller;

import com.example.travelez.backend.common.api.BaseResponse;
import com.example.travelez.backend.common.api.CommonPage;
import com.example.travelez.backend.common.api.ResultCode;
import com.example.travelez.backend.itinerary.dto.response.ItinerarySummaryResponse;
import com.example.travelez.backend.itinerary.dto.response.SharedUserSearchResponse;
import com.example.travelez.backend.itinerary.service.ItineraryManagementService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping("/{id}/shared-users/search")
    public ResponseEntity<BaseResponse<List<SharedUserSearchResponse>>> searchSharedUsers(
            @PathVariable Long id,
            @RequestParam String keyword) {

        List<SharedUserSearchResponse> result = itineraryManagementService.searchSharedUsers(id, keyword);
        return BaseResponse.success(result, ResultCode.SUCCESS, "Search shared users successfully");
    }

    @GetMapping("/public/search")
    public ResponseEntity<BaseResponse<CommonPage<ItinerarySummaryResponse>>> searchItineraries(
            @RequestParam String prompt,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        CommonPage<ItinerarySummaryResponse> result = itineraryManagementService.searchPublicItineraries(prompt, pageable);

        return BaseResponse.success(result, ResultCode.SUCCESS, "Search success");
    }

    @PatchMapping("/{id}/public")
    public ResponseEntity<BaseResponse<Void>> togglePublicStatus(
            @PathVariable Long id,
            @RequestParam boolean isPublic) {
        itineraryManagementService.togglePublicStatus(id, isPublic);
        String message = isPublic ? "Itinerary is now public" : "Itinerary is now private";
        return BaseResponse.success(null, ResultCode.SUCCESS, message);
    }

    @GetMapping("/users/{userId}/public")
    public ResponseEntity<BaseResponse<CommonPage<ItinerarySummaryResponse>>> getUserPublicItineraries(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        CommonPage<ItinerarySummaryResponse> result = itineraryManagementService.getUserPublicItineraries(userId, pageable);

        return BaseResponse.success(result, ResultCode.SUCCESS, "Retrieve the list of successfully published itineraries.");
    }

    @GetMapping("/{id}/shared-users")
    public ResponseEntity<BaseResponse<CommonPage<SharedUserSearchResponse>>> getSharedUsers(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(
                page, size, Sort.by(Sort.Direction.DESC, "createdAt")
        );

        CommonPage<SharedUserSearchResponse> result = itineraryManagementService.getSharedUsers(id, pageable);
        return BaseResponse.success(result, ResultCode.SUCCESS, "Retrieve shared users list successfully");
    }

    @PostMapping("/{id}/export-calendar")
    public ResponseEntity<BaseResponse<Void>> exportToGoogleCalendar(@PathVariable Long id) {
        itineraryManagementService.exportToGoogleCalendar(id);
        return BaseResponse.success(null, ResultCode.SUCCESS, "Itinerary exported to Google Calendar successfully");
    }
}
