package com.example.travelez.backend.itinerary.controller;

import com.example.travelez.backend.common.api.BaseResponse;
import com.example.travelez.backend.common.api.CommonPage;
import com.example.travelez.backend.common.api.ResultCode;
import com.example.travelez.backend.common.dto.PaginationRequest;
import com.example.travelez.backend.common.utils.PaginationUtils;
import com.example.travelez.backend.itinerary.dto.request.ItineraryCreationRequest;
import com.example.travelez.backend.itinerary.dto.request.ItineraryReplanRequest;
import com.example.travelez.backend.itinerary.dto.request.ItinerarySaveRequest;
import com.example.travelez.backend.itinerary.dto.response.ItineraryDetailResponse;
import com.example.travelez.backend.itinerary.dto.response.ItineraryResponse;
import com.example.travelez.backend.itinerary.dto.response.ItinerarySummaryResponse;
import com.example.travelez.backend.itinerary.service.ItineraryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/itineraries")
@RequiredArgsConstructor
@Tag(name= "Itinerary - M1", description = "Itinerary endpoints")
public class ItineraryController {

    private final ItineraryService itineraryService;
    private final ObjectMapper objectMapper;

    @PostMapping("/generate")
    public ResponseEntity<BaseResponse<ItineraryResponse>> generateItinerary(
            @Valid @RequestBody ItineraryCreationRequest request) {

        // Gọi Service xử lý logic
        ItineraryResponse response = itineraryService.generateSmartItinerary(request);

        return BaseResponse.success(response, ResultCode.SUCCESS, "Itinerary created successfully");
    }

    @PostMapping("/save")
    public ResponseEntity<BaseResponse<Long>> saveItinerary(@RequestBody ItinerarySaveRequest request) {

        Long itineraryId = itineraryService.saveItinerary(request);

        return BaseResponse.success(itineraryId, ResultCode.SUCCESS, "Itinerary saved successfully");
    }

    @GetMapping()
    public ResponseEntity<BaseResponse<CommonPage<ItinerarySummaryResponse>>> getItinerarySummary(
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size) {

        final PaginationRequest request = new PaginationRequest(page, size, "createdAt", Sort.Direction.DESC);

        CommonPage<ItinerarySummaryResponse> response = itineraryService.getItineraryList(
                PaginationUtils.getPageable(request));

        return BaseResponse.success(response, ResultCode.SUCCESS, "Itinerary summary retrieved successfully");
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<ItineraryDetailResponse>> getItineraryDetail(@PathVariable Long id) {
        ItineraryDetailResponse response = itineraryService.getItineraryDetail(id);
        return BaseResponse.success(response, ResultCode.SUCCESS, "Itinerary details retrieved successfully");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<BaseResponse<Void>> deleteItinerary(@PathVariable Long id) {
        itineraryService.deleteItinerary(id);
        return BaseResponse.success(null, ResultCode.SUCCESS, "Itinerary deleted successfully");
    }

    @PostMapping("/replan")
    public ResponseEntity<BaseResponse<ItineraryResponse>> replanItinerary(
            @Valid @RequestBody ItineraryReplanRequest request) {

        ItineraryResponse response = itineraryService.replanSmartItinerary(request);

        return BaseResponse.success(response, ResultCode.SUCCESS, "Itinerary replanned successfully");
    }

    @GetMapping("/temp/{tempId}")
    public ResponseEntity<BaseResponse<ItineraryResponse>> getTempItinerary(@PathVariable String tempId) {
        ItineraryResponse response = itineraryService.getTempItinerary(tempId);
        return BaseResponse.success(response, ResultCode.SUCCESS, "Recovered temporary itinerary");
    }
}