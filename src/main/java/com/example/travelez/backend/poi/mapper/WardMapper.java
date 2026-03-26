package com.example.travelez.backend.poi.mapper;

import org.mapstruct.Mapper;

import com.example.travelez.backend.poi.dto.response.WardBaseResponse;
import com.example.travelez.backend.poi.model.Ward;

@Mapper(componentModel = "spring")
public interface WardMapper {
    WardBaseResponse toWardBaseResponse(Ward ward);
}
