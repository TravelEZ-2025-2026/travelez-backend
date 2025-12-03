package com.example.travelez.backend.poi.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.example.travelez.backend.media.mapper.MediaMapper;
import com.example.travelez.backend.poi.dto.response.PoiBaseResponse;
import com.example.travelez.backend.poi.dto.response.PoiDetailResponse;
import com.example.travelez.backend.poi.model.Poi;

@Mapper(componentModel = "spring", uses = { MediaMapper.class })
public interface PoiMapper {

    @Mapping(target = "medias", source = "medias")
    PoiDetailResponse toPoiDetailResponse(Poi poi);

    PoiBaseResponse toPoiBaseResponse(Poi poi);
}
