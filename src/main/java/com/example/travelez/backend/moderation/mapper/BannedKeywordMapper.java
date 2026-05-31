package com.example.travelez.backend.moderation.mapper;

import com.example.travelez.backend.moderation.dto.request.BannedKeywordCreateRequest;
import com.example.travelez.backend.moderation.dto.request.BannedKeywordUpdateRequest;
import com.example.travelez.backend.moderation.dto.response.BannedKeywordResponse;
import com.example.travelez.backend.moderation.model.BannedKeyword;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface BannedKeywordMapper {
    
    BannedKeyword toEntity(BannedKeywordCreateRequest request);
    
    BannedKeywordResponse toResponse(BannedKeyword entity);
    
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromRequest(BannedKeywordUpdateRequest request, @MappingTarget BannedKeyword entity);
}
