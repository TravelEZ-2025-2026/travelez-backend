package com.example.travelez.backend.media.mapper;

import com.example.travelez.backend.infrastructure.filestorage.dto.UploadFileResult;
import com.example.travelez.backend.media.dto.response.MediaBaseResponse;
import com.example.travelez.backend.media.model.Media;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MediaMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "url", source = "publicUrl")
    @Mapping(target = "type", source = "mediaType")
    Media toMedia(UploadFileResult dto);

    MediaBaseResponse toMediaBaseResponse(Media media);
}
