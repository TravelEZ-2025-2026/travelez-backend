package com.example.travelez.backend.poi.controller;

import com.example.travelez.backend.common.api.BaseResponse;
import com.example.travelez.backend.common.api.CommonPage;
import com.example.travelez.backend.common.api.ResultCode;
import com.example.travelez.backend.common.api.doc.ApiBaseResponses;
import com.example.travelez.backend.common.dto.PaginationRequest;
import com.example.travelez.backend.common.utils.PaginationUtils;
import com.example.travelez.backend.poi.dto.response.PlaceBaseResponse;
import com.example.travelez.backend.poi.service.PlaceService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/place")
@RequiredArgsConstructor
@Tag(name = "Place", description = "Place endpoints")
public class PlaceController {
    private final PlaceService placeService;

    @Operation(summary = "Find all places", description = "Find all places with optional filters")
    @ApiBaseResponses
    @ApiResponse(responseCode = "200", description = "Places fetched successfully")
    @GetMapping
    public ResponseEntity<BaseResponse<CommonPage<PlaceBaseResponse>>> findAllPlace(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String countryCode,
            @RequestParam(required = false, defaultValue = "id") String sortField,
            @RequestParam(required = false, defaultValue = "ASC") Sort.Direction sortDirection,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size) {
        final PaginationRequest request = new PaginationRequest(page, size, sortField, sortDirection);
        CommonPage<PlaceBaseResponse> pageResponse = placeService.findAllPlace(name, country, countryCode,
                PaginationUtils.getPageable(request));
        return BaseResponse.success(pageResponse, ResultCode.SUCCESS, "Places fetched successfully");
    }
}
