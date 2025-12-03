package com.example.travelez.backend.poi.service;

import com.example.travelez.backend.poi.dto.request.PoiFilterRequest;
import com.example.travelez.backend.poi.dto.response.PoiBaseResponse;
import com.example.travelez.backend.poi.dto.response.PoiDetailResponse;
import org.springframework.data.domain.Pageable;

import com.example.travelez.backend.common.api.CommonPage;

public interface PoiService {

    public CommonPage<PoiBaseResponse> findAllPoi(PoiFilterRequest request, Pageable pageable);

    public PoiDetailResponse getPoiDetail(long poiId);

}
