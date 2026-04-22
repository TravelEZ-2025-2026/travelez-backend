package com.example.travelez.backend.poi.service.impl;

import com.example.travelez.backend.common.api.CommonPage;
import com.example.travelez.backend.common.api.ResultCode;
import com.example.travelez.backend.common.exception.ApiException;
import com.example.travelez.backend.common.exception.Asserts;
import com.example.travelez.backend.infrastructure.gemini.GeminiEmbeddingService;
import com.example.travelez.backend.media.model.Media;
import com.example.travelez.backend.media.repository.MediaRepository;
import com.example.travelez.backend.media.service.MediaService;
import com.example.travelez.backend.poi.dto.request.PoiFilterRequest;
import com.example.travelez.backend.poi.dto.response.PoiBaseResponse;
import com.example.travelez.backend.poi.dto.response.PoiDetailResponse;
import com.example.travelez.backend.poi.mapper.PoiMapper;
import com.example.travelez.backend.poi.model.Place;
import com.example.travelez.backend.poi.model.Poi;
import com.example.travelez.backend.poi.model.enums.PlaceStatus;
import com.example.travelez.backend.poi.model.enums.PoiStatus;
import com.example.travelez.backend.poi.model.enums.PoiType;
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

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PoiServiceImpl implements PoiService {
    private final PoiRepository poiRepository;
    private final PoiMapper poiMapper;
    private final PlaceService placeService;
    private final MediaService mediaService;
    private final GeminiEmbeddingService geminiService;

    // service public
    public CommonPage<PoiBaseResponse> findAllPoi(PoiFilterRequest request, Pageable pageable) {

        List<Specification<Poi>> specs = new ArrayList<>();
        specs.add(PoiSpecification.filterBySystemStatus(PoiStatus.ACTIVE));
        specs.add(PoiSpecification.filterByPlaceId(request.getPlaceId()));
        specs.add(PoiSpecification.filterByWardId(request.getWardId()));
        specs.add(PoiSpecification.filterByName(request.getName()));
        specs.add(PoiSpecification.filterByPoiType(request.getPoiType()));
        specs.add(PoiSpecification.filterByPlaceStatus(request.getPlaceStatus()));
        specs.add(PoiSpecification.filterByRating(request.getRating()));

        Page<Poi> pois = poiRepository.findAll(Specification.allOf(specs), pageable);
        List<Long> poiIds = pois.getContent().stream().map(item -> item.getId()).toList();
        List<Object[]> mediaData = poiRepository.findAllMediasByPoiIds(poiIds);
        Map<Long, List<Media>> mediaMap = mediaService.groupMediaByParentId(mediaData);
        List<PoiBaseResponse> poiBaseResponses = pois.getContent().stream()
                .map(poi -> poiMapper.toPoiBaseResponse(poi, mediaMap.getOrDefault(poi.getId(), List.of())))
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
        Place place = placeService.getPlaceByCodename(codename);

        List<Poi> pois = poiRepository.findByPlaceAndSystemStatusAndStatus(
                place,
                PoiStatus.ACTIVE,
                PlaceStatus.OPERATIONAL
        );

        if (pois.isEmpty()) {
            Asserts.fail(ResultCode.NOT_FOUND, "No location data available for: " + place.getName());
        }

        return pois;
    }

    public List<PoiBaseResponse> semanticSearchPoi(String query, Long placeId, PoiType poiType, int limit) {
        List<float[]> embeddings = geminiService.embedTexts(List.of(query));

        if (embeddings == null || embeddings.isEmpty() || embeddings.get(0).length == 0) {
            log.warn("Cannot generate vector for semantic search query: {}", query);
            return List.of();
        }

        float[] vectorRaw = embeddings.get(0);
        String vectorString = Arrays.toString(vectorRaw);

        String poiTypeStr = (poiType != null) ? poiType.name() : null;

        List<Poi> pois = poiRepository.findBySemanticSearch(vectorString, placeId, poiTypeStr, limit);

        if (pois.isEmpty()) {
            return List.of();
        }

        List<Long> poiIds = pois.stream().map(Poi::getId).toList();
        List<Object[]> mediaData = poiRepository.findAllMediasByPoiIds(poiIds);
        Map<Long, List<Media>> mediaMap = mediaService.groupMediaByParentId(mediaData);

        return pois.stream()
                .map(poi -> poiMapper.toPoiBaseResponse(poi, mediaMap.getOrDefault(poi.getId(), List.of())))
                .toList();
    }

}
