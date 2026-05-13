package com.example.travelez.backend.enhancement.controller;

import com.example.travelez.backend.common.api.BaseResponse;
import com.example.travelez.backend.common.api.CommonPage;
import com.example.travelez.backend.common.api.ResultCode;
import com.example.travelez.backend.common.dto.PaginationRequest;
import com.example.travelez.backend.common.utils.PaginationUtils;
import com.example.travelez.backend.enhancement.dto.response.HistoryDetailResponse;
import com.example.travelez.backend.enhancement.dto.response.ItineraryEnhancementHistorySummary;
import com.example.travelez.backend.enhancement.facade.ItineraryEnhancementFacade;
import com.example.travelez.backend.enhancement.service.ItineraryEnhancementService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/itinerary-enhancement")
@Tag(name= "Itinerary enhancement for provider - M7", description = "Itinerary endpoints")
@RequiredArgsConstructor
public class ItineraryEnhancementController {
    private final ItineraryEnhancementFacade enhancementFacade;
    private final ItineraryEnhancementService enhancementService;

    @PostMapping(value = "/improve", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BaseResponse<Long>> analyzeItinerary(
            @RequestPart("file") MultipartFile documentFile,
            @RequestPart("providerPrompt") String providerPrompt) {

        Long historyId = enhancementFacade.analyzeAndEnhance(documentFile, providerPrompt);
        return BaseResponse.success(historyId, ResultCode.SUCCESS, "Analysis and enhancement completed successfully");
    }

    @GetMapping("/histories")
    public ResponseEntity<BaseResponse<CommonPage<ItineraryEnhancementHistorySummary>>> getMyHistories(
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size) {

        final PaginationRequest request = new PaginationRequest(page, size, "createdAt", Sort.Direction.DESC);

        CommonPage<ItineraryEnhancementHistorySummary> response = enhancementService.getMyHistories(
                PaginationUtils.getPageable(request)
        );

        return BaseResponse.success(response, ResultCode.SUCCESS, "Enhancement histories retrieved successfully");
    }

    @GetMapping("/histories/{id}")
    public ResponseEntity<BaseResponse<HistoryDetailResponse>> getHistoryDetail(@PathVariable Long id) {
        return BaseResponse.success(enhancementService.getHistoryDetail(id), ResultCode.SUCCESS, "Enhancement history detail retrieved successfully");
    }

    @DeleteMapping("/histories/{id}")
    public ResponseEntity<BaseResponse<Void>> deleteHistory(@PathVariable Long id) {
        enhancementService.deleteHistory(id);
        return BaseResponse.success(null, ResultCode.SUCCESS, "Enhancement history deleted successfully");
    }
}
