package com.example.travelez.backend.enhancement.pipeline;

import com.example.travelez.backend.common.api.ResultCode;
import com.example.travelez.backend.common.exception.ApiException;
import com.example.travelez.backend.enhancement.dto.response.ItineraryEnhancementResponse;
import com.example.travelez.backend.enhancement.model.ItineraryEnhancementContext;
import com.example.travelez.backend.infrastructure.gemini.GeminiService;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class Phase4Analysis {
    private final GeminiService geminiService;
    private final Gson gson;

    public ItineraryEnhancementResponse execute(ItineraryEnhancementContext context) {
        log.info("Phase 4: Injecting data into AI Prompt and analyzing B2B itinerary...");

        String prompt = buildSystemPrompt(context);

        String jsonResult = geminiService.generateJson(prompt, GeminiService.ModelType.FLASH);
        context.setLlmFinalJsonResponse(jsonResult);

        try {
            String cleanJson = jsonResult.replaceAll("(?s)^[^{]*(\\{.*\\})[^}]*$", "$1");
            ItineraryEnhancementResponse response = gson.fromJson(cleanJson, ItineraryEnhancementResponse.class);
            log.info("Phase 4: Analysis Pipeline completed.");
            return response;
        } catch (Exception e) {
            log.error("AI Parse Error for Itinerary Enhancement: {}", jsonResult, e);
            throw new ApiException(ResultCode.AI_RESPONSE_FORMAT_ERROR, "Unable to convert JSON format from Agent AI");
        }
    }

    private String buildSystemPrompt(ItineraryEnhancementContext context) {
        String knownInsightsStr = gson.toJson(context.getKnownPoiInsights());
        String unknownPoisStr = gson.toJson(context.getUnknownPoiNames());
        String recommendationPoolStr = gson.toJson(context.getRecommendationPool());
        String providerPrompt = context.getProviderPrompt();

        return String.format("""
                Context: You are a Senior B2B Itinerary Strategy Consulting Expert. 
                Your task is to evaluate the itinerary below and provide professional upgrade recommendations without subjective judgment.

                INPUT DATA:
                1. PROVIDER'S GOALS: %s
                2. IN-SYSTEM POI INSIGHTS (Based on actual data): %s
                3. OUT-OF-SYSTEM / UNKNOWN POIS (Needs verification): %s
                4. RECOMMENDATION POOL (To fill experience gaps): %s

                ANALYSIS PRINCIPLES (MUST COMPLY STRICTLY):
                
                1. PROFESSIONAL B2B TONE:
                   - ABSOLUTELY DO NOT use extreme words like: "hoàn toàn không phù hợp", "bị già quá", "chán", "vô lý".
                   - Use refined consulting language like: "chưa thực sự tối ưu cho tệp khách...", "mang màu sắc truyền thống đậm nét nên có thể kén khách trẻ", "cần cân nhắc về tính trải nghiệm".
                
                2. CONCISE & FOCUSED (VERBOSITY CONTROL):
                   - "itineraryPros": Maximum of 3 most outstanding strengths.
                   - "experienceGaps": Maximum of 3 biggest gaps based on the Provider's goals.
                   - "suggestedAdditions": ONLY select a maximum of 3 top locations from Section 4 to fill the gaps.
                   - "externalPoiAdvisories": ABSOLUTELY IGNORE common nouns (like "Sài Gòn", "Quận 1", "TP.HCM", "Sông"). Only provide advice for specific out-of-system places like restaurants or venues.
                
                3. ENTITY DEDUPLICATION:
                   - If locations have similar names (e.g., "Địa đạo Củ Chi" and "Củ Chi - Bến Dược"), merge the insights to avoid repetition.
                   
                4. LANGUAGE REQUIREMENT (CRITICAL):
                   - All text values inside the JSON output MUST BE WRITTEN IN VIETNAMESE. 

                5. RETURN JSON STRUCTURE:
                {
                  "itineraryPros": ["..."],
                  "experienceGaps": ["..."],
                  "actionableEnhancements": [
                    { "poiName": "...", "insightType": "...", "evidence": "... mentions", "advice": "..." }
                  ],
                  "suggestedAdditions": [
                    { "proposedPoi": "...", "targetGap": "...", "evidence": "... mentions", "reason": "..." }
                  ],
                  "externalPoiAdvisories": [
                     { "poiName": "...", "advisory": "..." }
                  ]
                }
                """, providerPrompt, knownInsightsStr, unknownPoisStr, recommendationPoolStr);
    }

}
