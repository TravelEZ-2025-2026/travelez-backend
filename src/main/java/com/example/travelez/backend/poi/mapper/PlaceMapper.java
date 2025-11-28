package com.example.travelez.backend.poi.mapper;

import org.mapstruct.Mapper;

import com.example.travelez.backend.poi.dto.response.PlaceBaseResponse;
import com.example.travelez.backend.poi.model.Place;

@Mapper(componentModel = "spring")
public interface PlaceMapper {
    PlaceBaseResponse toPlaceBaseResponse(Place place);
}
