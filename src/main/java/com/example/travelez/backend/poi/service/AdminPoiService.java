package com.example.travelez.backend.poi.service;

import com.example.travelez.backend.common.api.CommonPage;
import com.example.travelez.backend.poi.dto.request.AdminPoiFilterRequest;
import com.example.travelez.backend.poi.dto.response.AdminPoiDetailResponse;
import com.example.travelez.backend.poi.dto.response.AdminPoiResponse;
import com.example.travelez.backend.poi.dto.response.PoiStatResponse;

import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;

@PreAuthorize("hasRole('ADMIN')")
public interface AdminPoiService {
    PoiStatResponse getStatistics(Long placeId, Long wardId);
    CommonPage<AdminPoiResponse> getAllPois(AdminPoiFilterRequest request, Pageable pageable);
    AdminPoiDetailResponse getPoiDetail(long poiId);
//    PoiDetailResponse updatePoi(Long id, AdminPoiUpdateRequest request);
    void deletePoi(long id);
}
