package com.example.travelez.backend.media.mapper;

import org.mapstruct.Mapper;

import com.example.travelez.backend.media.dto.response.MediaBaseResponse;
import com.example.travelez.backend.media.model.Media;

@Mapper(componentModel = "spring")
public interface MediaMapper {
    MediaBaseResponse toMediaBaseResponse(Media media);
}
