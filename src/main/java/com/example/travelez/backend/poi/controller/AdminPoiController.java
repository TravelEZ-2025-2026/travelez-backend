package com.example.travelez.backend.poi.controller;

import com.example.travelez.backend.common.api.BaseResponse;
import com.example.travelez.backend.common.api.CommonPage;
import com.example.travelez.backend.common.api.ResultCode;
import com.example.travelez.backend.common.dto.PaginationRequest;
import com.example.travelez.backend.common.utils.PaginationUtils;
import com.example.travelez.backend.poi.dto.request.AdminPoiFilterRequest;
import com.example.travelez.backend.poi.dto.response.AdminPoiDetailResponse;
import com.example.travelez.backend.poi.dto.response.AdminPoiResponse;
import com.example.travelez.backend.poi.dto.response.PoiStatResponse;
import com.example.travelez.backend.poi.service.AdminPoiService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/pois")
@RequiredArgsConstructor
@Tag(name = "Admin POI Management", description = "Admin POI endpoints")
public class AdminPoiController {
    private final AdminPoiService adminPoiService;

    @GetMapping("/stats")
    public ResponseEntity<BaseResponse<PoiStatResponse>> getStats(
            @RequestParam(required = false) Long placeId,
            @RequestParam(required = false) Long wardId) {
        return BaseResponse.success(adminPoiService.getStatistics(placeId, wardId), ResultCode.SUCCESS, "Statistics fetched successfully");
    }

    // get list pois
    @GetMapping
    public ResponseEntity<BaseResponse<CommonPage<AdminPoiResponse>>> getAllPois(
            @ParameterObject AdminPoiFilterRequest request,
            @RequestParam(required = false, defaultValue = "id") String sortField,
            @RequestParam(required = false, defaultValue = "ASC") Sort.Direction sortDirection,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size) {
        final PaginationRequest pageable = new PaginationRequest(page, size, sortField, sortDirection);
        CommonPage<AdminPoiResponse> response = adminPoiService.getAllPois(request, PaginationUtils.getPageable(pageable));
        return BaseResponse.success(response, ResultCode.SUCCESS, "Pois fetched successfully");
    }

    @GetMapping("/{poiId}")
    public ResponseEntity<BaseResponse<AdminPoiDetailResponse>> getPoiDetail(@PathVariable long poiId) {
        return BaseResponse.success(adminPoiService.getPoiDetail(poiId), ResultCode.SUCCESS, "Poi fetched successfully");
    }

    @DeleteMapping("/{poiId}")
    public ResponseEntity<BaseResponse<Void>> deletePoi(@PathVariable long poiId) {
        adminPoiService.deletePoi(poiId);
        return BaseResponse.success(null, ResultCode.SUCCESS, "Poi deleted successfully");
    }
}
