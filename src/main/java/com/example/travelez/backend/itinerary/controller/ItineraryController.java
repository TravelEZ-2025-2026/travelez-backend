package com.example.travelez.backend.itinerary.controller;

import com.example.travelez.backend.common.api.BaseResponse;
import com.example.travelez.backend.common.api.ResultCode;
import com.example.travelez.backend.itinerary.dto.request.CreateItineraryRequest;
import com.example.travelez.backend.itinerary.dto.request.SaveItineraryRequest;
import com.example.travelez.backend.itinerary.dto.response.GetItineraryResponse;
import com.example.travelez.backend.itinerary.dto.response.ItineraryResponse;
import com.example.travelez.backend.itinerary.service.impl.ItineraryServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper; // Import thêm cái này
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/itinerary")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ItineraryController {

    private final ItineraryServiceImpl itineraryService;
    private final ObjectMapper objectMapper;

    @PostMapping("/generate")
    public ResponseEntity<BaseResponse<ItineraryResponse>> generateItinerary(
            @RequestBody CreateItineraryRequest request) {

        // Gọi Service xử lý logic
        ItineraryResponse response = itineraryService.generateSmartItinerary(request);

        return BaseResponse.success(response, ResultCode.SUCCESS, "Tạo lộ trình thành công");
    }

    @PostMapping("/save")
    public ResponseEntity<BaseResponse<Long>> saveItinerary(@RequestBody SaveItineraryRequest request) {

        Long itineraryId = itineraryService.saveItinerary(request);

        return BaseResponse.success(itineraryId, ResultCode.SUCCESS, "Lưu lộ trình thành công");
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<GetItineraryResponse>> getItineraryDetail(@PathVariable Long id) {
        GetItineraryResponse response = itineraryService.getItineraryDetail(id);
        return BaseResponse.success(response, ResultCode.SUCCESS, "Lấy chi tiết lộ trình thành công");
    }
}