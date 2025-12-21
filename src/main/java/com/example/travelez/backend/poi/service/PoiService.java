package com.example.travelez.backend.poi.service;

import com.example.travelez.backend.poi.dto.request.PoiFilterRequest;
import com.example.travelez.backend.poi.dto.response.PoiBaseResponse;
import com.example.travelez.backend.poi.dto.response.PoiDetailResponse;
import com.example.travelez.backend.poi.model.Poi;
import com.example.travelez.backend.poi.model.enums.PoiStatus;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;

import com.example.travelez.backend.common.api.CommonPage;

public interface PoiService {

    public CommonPage<PoiBaseResponse> findAllPoi(PoiFilterRequest request, Pageable pageable);

    public PoiDetailResponse getPoiDetail(long poiId);

    public Optional<Poi> findByIdAndSystemStatus(long poiId, PoiStatus systemStatus);

    public List<Poi> getActivePoisByCity(String codename);

}
