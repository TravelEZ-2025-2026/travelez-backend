package com.example.travelez.backend.itinerary.controller;

import com.example.travelez.backend.common.api.BaseResponse;
import com.example.travelez.backend.common.api.ResultCode;
import com.example.travelez.backend.itinerary.dto.request.ItineraryCreationRequest;
import com.example.travelez.backend.itinerary.dto.request.ItinerarySaveRequest;
import com.example.travelez.backend.itinerary.dto.response.ItineraryDetailResponse;
import com.example.travelez.backend.itinerary.dto.response.ItineraryResponse;
import com.example.travelez.backend.itinerary.service.impl.ItineraryServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper; // Import thêm cái này
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/itinerary")
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

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<ItineraryDetailResponse>> getItineraryDetail(@PathVariable Long id) {
        ItineraryDetailResponse response = itineraryService.getItineraryDetail(id);
        return BaseResponse.success(response, ResultCode.SUCCESS, "Itinerary details retrieved successfully");
    }
}