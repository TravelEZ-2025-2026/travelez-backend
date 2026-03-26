package com.example.travelez.backend.poi.service.impl;

import com.example.travelez.backend.common.api.CommonPage;
import com.example.travelez.backend.common.api.ResultCode;
import com.example.travelez.backend.common.exception.ApiException;
import com.example.travelez.backend.poi.dto.response.PlaceBaseResponse;
import com.example.travelez.backend.poi.dto.response.WardBaseResponse;
import com.example.travelez.backend.poi.mapper.PlaceMapper;
import com.example.travelez.backend.poi.mapper.WardMapper;
import com.example.travelez.backend.poi.model.Place;
import com.example.travelez.backend.poi.repository.PlaceRepository;
import com.example.travelez.backend.poi.repository.WardRepository;
import com.example.travelez.backend.poi.repository.specification.PlaceSpecification;
import com.example.travelez.backend.poi.service.PlaceService;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlaceServiceImpl implements PlaceService {
    private final PlaceRepository placeRepository;
    private final WardRepository wardRepository;
    private final PlaceMapper placeMapper;
    private final WardMapper wardMapper;

    public CommonPage<PlaceBaseResponse> findAllPlace(String name, String country, String countryCode,
            Pageable pageable) {
        try {
            List<Specification<Place>> specs = new ArrayList<>();
            specs.add(PlaceSpecification.filterByName(name));
            specs.add(PlaceSpecification.filterByCountry(country));
            specs.add(PlaceSpecification.filterByCountryCode(countryCode));
            Page<Place> places = placeRepository.findAll(Specification.allOf(specs), pageable);
            List<PlaceBaseResponse> placeBaseResponses = places.stream()
                    .map(placeMapper::toPlaceBaseResponse)
                    .toList();
            return new CommonPage<>(placeBaseResponses, places.getTotalPages(), places.getTotalElements(),
                    pageable.getPageSize(), places.getNumber(), places.isEmpty());
        } catch (Exception e) {
            throw new ApiException(ResultCode.INTERNAL_SERVER_ERROR, "Error fetching places");
        }
    }

    @Override
    public Place getPlaceByCodename(String codeName) {
        return placeRepository.findByCodename(codeName)
                .orElseThrow(() -> new ApiException(
                        ResultCode.NOT_FOUND, "City not found: " + codeName
                ));
    }

    @Override
    public List<WardBaseResponse> getAllWardsOfPlace(Long placeId) {
        return wardRepository.findAllByPlace_Id(placeId).stream()
                .map(wardMapper::toWardBaseResponse)
                .toList();
    }
}
