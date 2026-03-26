package com.example.travelez.backend.poi.service;

import com.example.travelez.backend.common.api.CommonPage;
import com.example.travelez.backend.poi.dto.response.PlaceBaseResponse;
import com.example.travelez.backend.poi.dto.response.WardBaseResponse;
import com.example.travelez.backend.poi.model.Place;

import java.util.List;

import org.springframework.data.domain.Pageable;

public interface PlaceService {

    public CommonPage<PlaceBaseResponse> findAllPlace(String name, String country, String countryCode,
            Pageable pageable);

    public Place getPlaceByCodename(String codename);

    public List<WardBaseResponse> getAllWardsOfPlace(Long placeId);
}
