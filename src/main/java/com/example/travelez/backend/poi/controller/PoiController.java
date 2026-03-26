package com.example.travelez.backend.poi.controller;

import com.example.travelez.backend.common.api.BaseResponse;
import com.example.travelez.backend.common.api.CommonPage;
import com.example.travelez.backend.common.api.ResultCode;
import com.example.travelez.backend.common.api.doc.ApiBaseResponses;
import com.example.travelez.backend.common.api.doc.ApiNotFoundResponses;
import com.example.travelez.backend.common.dto.PaginationRequest;
import com.example.travelez.backend.common.utils.PaginationUtils;
import com.example.travelez.backend.poi.dto.request.PoiFilterRequest;
import com.example.travelez.backend.poi.dto.response.PoiBaseResponse;
import com.example.travelez.backend.poi.dto.response.PoiDetailResponse;
import com.example.travelez.backend.poi.model.enums.PlaceStatus;
import com.example.travelez.backend.poi.model.enums.PoiType;
import com.example.travelez.backend.poi.service.PoiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pois")
@RequiredArgsConstructor
@Tag(name = "POI", description = "POI endpoints")
public class PoiController {

    private final PoiService poiService;

    @Operation(summary = "Find all POIs", description = "Find all POIs with optional filters")
    @ApiBaseResponses
    @ApiResponse(responseCode = "200", description = "Pois fetched successfully")
    @GetMapping
    public ResponseEntity<BaseResponse<CommonPage<PoiBaseResponse>>> findAllPoi(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long placeId,
            @RequestParam(required = false) Long wardId,
            @RequestParam(required = false) PoiType poiType,
            @RequestParam(required = false) PlaceStatus placeStatus,
            @RequestParam(required = false) Double rating,
            @RequestParam(required = false, defaultValue = "id") String sortField,
            @RequestParam(required = false, defaultValue = "ASC") Sort.Direction sortDirection,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size) {

        final PaginationRequest request = new PaginationRequest(page, size, sortField, sortDirection);

        PoiFilterRequest poiFilterRequest = PoiFilterRequest.builder()
                .placeId(placeId)
                .wardId(wardId)
                .name(name)
                .poiType(poiType)
                .placeStatus(placeStatus)
                .rating(rating)
                .build();
        CommonPage<PoiBaseResponse> pageResponse = poiService.findAllPoi(poiFilterRequest,
                PaginationUtils.getPageable(request));

        return BaseResponse.success(pageResponse,
                ResultCode.SUCCESS, "Pois fetched successfully");
    }

    @Operation(summary = "Get POI detail", description = "Get POI detail by ID")
    @ApiBaseResponses
    @ApiNotFoundResponses
    @ApiResponse(responseCode = "200", description = "Pois fetched successfully")
    @GetMapping("/{poiId}")
    public ResponseEntity<BaseResponse<PoiDetailResponse>> getPoi(@PathVariable long poiId) {
        return BaseResponse.success(poiService.getPoiDetail(poiId), ResultCode.SUCCESS,
                "Poi fetched successfully");
    }
}