package com.example.travelez.backend.poi.mapper;

import com.example.travelez.backend.media.mapper.MediaMapper;
import com.example.travelez.backend.media.model.Media;
import com.example.travelez.backend.poi.dto.response.PoiBaseResponse;
import com.example.travelez.backend.poi.dto.response.PoiDetailResponse;
import com.example.travelez.backend.poi.model.Poi;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = {MediaMapper.class})
public interface PoiMapper {

    PoiDetailResponse toPoiDetailResponse(Poi poi);

    @Mapping(target = "medias", source = "medias")
    PoiBaseResponse toPoiBaseResponse(Poi poi, List<Media> medias);
}
