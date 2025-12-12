package com.example.travelez.backend.ai.service.impl;

import com.example.travelez.backend.ai.prompt.ItineraryPromptBuilder;
import com.example.travelez.backend.ai.service.AiService;
import com.example.travelez.backend.common.api.ResultCode;
import com.example.travelez.backend.common.exception.ApiException;
import com.example.travelez.backend.infrastructure.gemini.GeminiService;
import com.example.travelez.backend.itinerary.dto.request.ItineraryCreationRequest;
import com.example.travelez.backend.itinerary.dto.response.ItineraryResponse;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiServiceImpl implements AiService {
    private final GeminiService geminiService;
    private final ItineraryPromptBuilder itineraryPromptBuilder;
    private final Gson gson;

    @Override
    public ItineraryResponse generateItinerary(ItineraryCreationRequest request, String poiContextJson) {
        String prompt = itineraryPromptBuilder.buildPrompt(request, poiContextJson);

        String jsonResult = geminiService.generateJson(prompt, GeminiService.ModelType.FLASH);

        try {
            String cleanJson = jsonResult.replaceAll("\\s*[\\(\\[](?i)(?:ID\\s*)?\\d+[\\)\\]]", "");
            return gson.fromJson(cleanJson, ItineraryResponse.class);
        } catch (Exception e) {
            log.error("AI Parse Error for Itinerary: {}", jsonResult, e);
            throw new ApiException(ResultCode.AI_RESPONSE_FORMAT_ERROR);
        }
    }
}
