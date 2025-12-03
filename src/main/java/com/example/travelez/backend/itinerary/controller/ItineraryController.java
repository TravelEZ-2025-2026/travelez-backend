package com.example.travelez.backend.itinerary.controller;

import com.example.travelez.backend.common.api.BaseResponse;
import com.example.travelez.backend.itinerary.dto.ItineraryRequest;
import com.example.travelez.backend.itinerary.service.ItineraryService;
import com.fasterxml.jackson.databind.ObjectMapper; // Import thêm cái này
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/itinerary")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ItineraryController {

    private final ItineraryService itineraryService;
    private final ObjectMapper objectMapper; // Inject ObjectMapper có sẵn của Spring

    @PostMapping("/plan")
    public ResponseEntity<BaseResponse<Object>> planTrip(@RequestBody ItineraryRequest request) {

        // 1. Lấy chuỗi JSON từ Service (vẫn là String)
        String resultJsonString = itineraryService.planTrip(request.getPrompt());

        try {
            // 2. BIẾN HÓA: Parse chuỗi String đó thành Object (Map hoặc JsonNode)
            // Jackson sẽ biến "{\n \"trip_title\": ... }" thành Object Java xịn
            Object jsonObject = objectMapper.readValue(resultJsonString, Object.class);

            // 3. Trả về Object đó. Spring sẽ serialize nó thành JSON chuẩn (không bị escape)
            return BaseResponse.success(jsonObject);

        } catch (Exception e) {
            // Trường hợp AI trả về bậy bạ không parse được, trả về dạng String để debug
            return BaseResponse.success(resultJsonString);
        }
    }
}