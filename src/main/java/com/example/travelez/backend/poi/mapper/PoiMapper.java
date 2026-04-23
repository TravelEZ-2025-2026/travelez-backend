package com.example.travelez.backend.poi.mapper;

import com.example.travelez.backend.media.mapper.MediaMapper;
import com.example.travelez.backend.media.model.Media;
import com.example.travelez.backend.poi.dto.response.AdminPoiDetailResponse;
import com.example.travelez.backend.poi.dto.response.AdminPoiResponse;
import com.example.travelez.backend.poi.dto.response.PoiBaseResponse;
import com.example.travelez.backend.poi.dto.response.PoiDetailResponse;
import com.example.travelez.backend.poi.dto.response.PoiSummaryResponse;
import com.example.travelez.backend.poi.model.Poi;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = {MediaMapper.class, PlaceMapper.class, WardMapper.class})
public interface PoiMapper {

    PoiDetailResponse toPoiDetailResponse(Poi poi);

    @Mapping(target = "medias", source = "medias")
    PoiBaseResponse toPoiBaseResponse(Poi poi, List<Media> medias);

    @Mapping(target = "medias", source = "medias")
    AdminPoiResponse toAdminPoiResponse(Poi poi, List<Media> medias);

    AdminPoiDetailResponse toAdminPoiDetailResponse(Poi poi);

    PoiSummaryResponse toPoiSummaryResponse(Poi poi);
}
