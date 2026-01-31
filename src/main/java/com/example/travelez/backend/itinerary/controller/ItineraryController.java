package com.example.travelez.backend.itinerary.controller;

import com.example.travelez.backend.common.api.BaseResponse;
import com.example.travelez.backend.common.api.CommonPage;
import com.example.travelez.backend.common.api.ResultCode;
import com.example.travelez.backend.common.dto.PaginationRequest;
import com.example.travelez.backend.common.utils.PaginationUtils;
import com.example.travelez.backend.itinerary.dto.request.ItineraryCreationRequest;
import com.example.travelez.backend.itinerary.dto.request.ItinerarySaveRequest;
import com.example.travelez.backend.itinerary.dto.response.ItineraryDetailResponse;
import com.example.travelez.backend.itinerary.dto.response.ItineraryResponse;
import com.example.travelez.backend.itinerary.dto.response.ItinerarySummaryResponse;
import com.example.travelez.backend.itinerary.service.impl.ItineraryServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/itineraries")
@RequiredArgsConstructor
public class ItineraryController {

    private final ItineraryServiceImpl itineraryService;
    private final ObjectMapper objectMapper;

    @PostMapping("/generate")
    public ResponseEntity<BaseResponse<ItineraryResponse>> generateItinerary(
            @RequestBody ItineraryCreationRequest request) {

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
}