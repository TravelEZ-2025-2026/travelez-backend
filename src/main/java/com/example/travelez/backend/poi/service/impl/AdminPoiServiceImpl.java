package com.example.travelez.backend.poi.service.impl;

import com.example.travelez.backend.common.api.CommonPage;
import com.example.travelez.backend.common.api.ResultCode;
import com.example.travelez.backend.common.exception.ApiException;
import com.example.travelez.backend.common.exception.Asserts;
import com.example.travelez.backend.dashboard.model.enums.ActivityCategory;
import com.example.travelez.backend.dashboard.service.impl.AuditLogService;
import com.example.travelez.backend.media.model.Media;
import com.example.travelez.backend.media.service.MediaService;
import com.example.travelez.backend.poi.dto.request.AdminPoiFilterRequest;
import com.example.travelez.backend.poi.dto.response.AdminPoiDetailResponse;
import com.example.travelez.backend.poi.dto.response.AdminPoiResponse;
import com.example.travelez.backend.poi.dto.response.PoiStatResponse;
import com.example.travelez.backend.poi.mapper.PoiMapper;
import com.example.travelez.backend.poi.model.Poi;
import com.example.travelez.backend.poi.repository.PoiRepository;
import com.example.travelez.backend.poi.repository.projection.GeneralStatPoiProjection;
import com.example.travelez.backend.poi.repository.specification.PoiSpecification;
import com.example.travelez.backend.poi.service.AdminPoiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminPoiServiceImpl implements AdminPoiService {
    private final PoiRepository poiRepository;
    private final MediaService mediaService;

    private final PoiMapper poiMapper;

    private final AuditLogService auditLogService;

    @Override
    public PoiStatResponse getStatistics(Long placeId, Long wardId) {
        GeneralStatPoiProjection generalData = poiRepository.getGeneralStats(placeId, wardId);
        List<Object[]> typeDataList = poiRepository.getCountGroupByType(placeId, wardId);

        Map<String, Long> typeMap = new HashMap<>();
        for (Object[] row : typeDataList) {
            String typeName = row[0] != null ? row[0].toString() : "OTHER";
            typeMap.put(typeName, (Long) row[1]);
        }

        return PoiStatResponse.builder()
                .totalPoi(generalData.getTotal())
                .activePoi(generalData.getActiveCount())
                .bannedPoi(generalData.getBannedCount())
                .operationalPoi(generalData.getOperationalCount())
                .closedPoi(generalData.getClosedCount())
                .totalReviews(generalData.getTotalReview())
                .averageRating(generalData.getAverageRating())
                .poisByType(typeMap)
                .build();
    }

    @Override
    public CommonPage<AdminPoiResponse> getAllPois(AdminPoiFilterRequest request, Pageable pageable) {
        List<Specification<Poi>> specs = new ArrayList<>();
        specs.add(PoiSpecification.filterByPlaceId(request.getPlaceId()));
        specs.add(PoiSpecification.filterByWardId(request.getWardId()));
        specs.add(PoiSpecification.filterByName(request.getName()));
        specs.add(PoiSpecification.filterByPoiType(request.getPoiType()));
        specs.add(PoiSpecification.filterBySystemStatus(request.getPoiStatus()));
        specs.add(PoiSpecification.filterByGreaterThanRating(request.getRating()));

        Page<Poi> page = poiRepository.findAll(Specification.allOf(specs), pageable);
        List<Long> poiIds = page.getContent().stream().map(Poi::getId).toList();
        List<Object[]> mediaData = poiRepository.findAllMediasByPoiIds(poiIds);
        Map<Long, List<Media>> mediaMap = mediaService.groupMediaByParentId(mediaData);
        List<AdminPoiResponse> adminPoiResponses = page.getContent().stream()
                .map(poi -> poiMapper.toAdminPoiResponse(poi, mediaMap.getOrDefault(poi.getId(), List.of())))
                .toList();

        return new CommonPage<>(adminPoiResponses, page.getTotalPages(), page.getTotalElements(), pageable.getPageSize(), page.getNumber(), page.isEmpty());
    }

    @Override
    public AdminPoiDetailResponse getPoiDetail(long poiId) {
        Poi poi = poiRepository.findByPoiId(poiId)
                .orElseThrow(() -> new ApiException(ResultCode.NOT_FOUND, "Poi not found"));
        return poiMapper.toAdminPoiDetailResponse(poi);
    }

    @Override   
    @Transactional
    public void deletePoi(long poiId) {
        boolean exists = poiRepository.existsById(poiId);
        if (!exists) {
            Asserts.fail(ResultCode.NOT_FOUND, "Poi not found");
        }
        poiRepository.softDeleteById(poiId);

        auditLogService.logActivity(
                ActivityCategory.SYSTEM,
                "Admin manually deleted POI ID #" + poiId + " from the system",
                "Action Taken"
        );
    }

}
