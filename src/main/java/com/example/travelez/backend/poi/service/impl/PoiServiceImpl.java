package com.example.travelez.backend.poi.service.impl;

import com.example.travelez.backend.common.api.CommonPage;
import com.example.travelez.backend.common.api.ResultCode;
import com.example.travelez.backend.common.exception.ApiException;
import com.example.travelez.backend.common.exception.Asserts;
import com.example.travelez.backend.common.exception.ErrorCode;
import com.example.travelez.backend.poi.dto.request.PoiFilterRequest;
import com.example.travelez.backend.poi.dto.response.PoiBaseResponse;
import com.example.travelez.backend.poi.dto.response.PoiDetailResponse;
import com.example.travelez.backend.poi.mapper.PoiMapper;
import com.example.travelez.backend.poi.model.Place;
import com.example.travelez.backend.poi.model.Poi;
import com.example.travelez.backend.poi.model.enums.PlaceStatus;
import com.example.travelez.backend.poi.model.enums.PoiStatus;
import com.example.travelez.backend.poi.repository.PoiRepository;
import com.example.travelez.backend.poi.repository.specification.PoiSpecification;
import com.example.travelez.backend.poi.service.PlaceService;
import com.example.travelez.backend.poi.service.PoiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PoiServiceImpl implements PoiService {
    private final PoiRepository poiRepository;
    private final PoiMapper poiMapper;
    private final PlaceService placeService;

    // service public
    public CommonPage<PoiBaseResponse> findAllPoi(PoiFilterRequest request, Pageable pageable) {

        List<Specification<Poi>> specs = new ArrayList<>();
        specs.add(PoiSpecification.filterBySystemStatus(PoiStatus.ACTIVE));
        specs.add(PoiSpecification.filterByPlaceId(request.getPlaceId()));
        specs.add(PoiSpecification.filterByName(request.getName()));
        specs.add(PoiSpecification.filterByPoiType(request.getPoiType()));
        specs.add(PoiSpecification.filterByPlaceStatus(request.getPlaceStatus()));
        specs.add(PoiSpecification.filterByRating(request.getRating()));

        Page<Poi> pois = poiRepository.findAll(Specification.allOf(specs), pageable);
        List<PoiBaseResponse> poiBaseResponses = pois.stream()
                .map(poiMapper::toPoiBaseResponse)
                .toList();

        return new CommonPage<>(poiBaseResponses, pois.getTotalPages(), pois.getTotalElements(),
                pageable.getPageSize(), pois.getNumber(), pois.isEmpty());

    }

    // service public
    public PoiDetailResponse getPoiDetail(long poiId) {
        Poi poi = poiRepository.findByIdAndSystemStatus(poiId, PoiStatus.ACTIVE)
                .orElseThrow(() -> new ApiException(ResultCode.NOT_FOUND, "Poi not found"));
        return poiMapper.toPoiDetailResponse(poi);
    }

    public Optional<Poi> findByIdAndSystemStatus(long poiId, PoiStatus systemStatus) {
        return poiRepository.findByIdAndSystemStatus(poiId, systemStatus);
    }

    @Override
    public List<Poi> getActivePoisByCity(String codename) {
        // 1. Gọi sang Place Domain
        Place place = placeService.getPlaceByCodename(codename);

        // 2. Query POI & Filter Status (Chỉ lấy quán đang hoạt động)
        List<Poi> pois = poiRepository.findByPlaceAndSystemStatusAndStatus(
                place,
                PoiStatus.ACTIVE,
                PlaceStatus.OPERATIONAL
        );

        if (pois.isEmpty()) {
            Asserts.fail(ErrorCode.NO_ACTIVE_POIS, "Chưa có dữ liệu địa điểm cho " + place.getName());
        }

        return pois;
    }
}
