package com.example.travelez.backend.moderation.mapper;

import com.example.travelez.backend.moderation.dto.response.ModerationAlertResponse;
import com.example.travelez.backend.moderation.model.ModerationAlert;
import com.example.travelez.backend.users.mapper.UserMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface ModerationAlertMapper {
    
    @Mapping(target = "reviewedBy", source = "reviewedBy")
    ModerationAlertResponse toResponse(ModerationAlert entity);
}
