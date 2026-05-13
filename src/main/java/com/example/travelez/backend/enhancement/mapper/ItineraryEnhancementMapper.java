package com.example.travelez.backend.enhancement.mapper;

import com.example.travelez.backend.enhancement.dto.response.ItineraryEnhancementHistorySummary;
import com.example.travelez.backend.enhancement.dto.response.ItineraryEnhancementResponse;
import com.example.travelez.backend.enhancement.model.ItineraryEnhancementHistory;
import com.example.travelez.backend.users.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ItineraryEnhancementMapper {
    ItineraryEnhancementHistorySummary toSummary(ItineraryEnhancementHistory entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "provider", source = "provider")
    @Mapping(target = "originalFileName", source = "fileName")
    @Mapping(target = "providerPrompt", source = "prompt")
    @Mapping(target = "analysisResult", source = "result")
    ItineraryEnhancementHistory toEntity(User provider,
                                         String fileName,
                                         String prompt,
                                         ItineraryEnhancementResponse result);

}
