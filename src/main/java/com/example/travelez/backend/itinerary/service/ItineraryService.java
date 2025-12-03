package com.example.travelez.backend.itinerary.service;

import com.example.travelez.backend.common.exception.ApiException;
import com.example.travelez.backend.common.exception.ErrorCode;
import com.example.travelez.backend.common.service.GeminiService;
import com.example.travelez.backend.itinerary.dto.ParsedIntent;
import com.example.travelez.backend.itinerary.model.PoiData;
import com.example.travelez.backend.itinerary.repository.MockPoiRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItineraryService {
    private final GeminiService geminiService;
    private final MockPoiRepository mockRepo;
    private final ObjectMapper objectMapper;

    public String planTrip(String userPrompt) {
        log.info("Start processing request: {}", userPrompt);

        // STEP 1: Analyze user intent (AI Call 1 - Flash)
        ParsedIntent intent = parseUserIntent(userPrompt);
        log.info("Parsed intent: Destination={}, Duration={} days", intent.getDestination(), intent.getDurationDays());

        // STEP 2: Fetch reference data (Mock DB)
        // Find POIs based on the destination analyzed by AI
        List<PoiData> candidatePois = mockRepo.findPoisByDestination(intent.getDestination());

        // Validate: If no data found, throw business exception immediately
        if (candidatePois.isEmpty()) {
            log.warn("No data found for destination: {}", intent.getDestination());
            throw new ApiException(ErrorCode.ITINERARY_GENERATION_FAILED,
                    "System currently has no data for location: " + intent.getDestination());
        }

        // STEP 3: Optimize and generate detailed itinerary (AI Call 2 - Pro)
        log.info("Generating itinerary with {} candidate POIs...", candidatePois.size());
        return generateItinerary(userPrompt, intent, candidatePois);
    }

    // -------------------------------------------------------
    // PRIVATE HELPER METHODS
    // -------------------------------------------------------

    private ParsedIntent parseUserIntent(String prompt) {
        try {
            // Prompt Engineering: Strictly extract JSON data
            String systemInstruction = """
                You are a strict JSON data extractor.
                Extract the following fields from the user input:
                - destination (String): city name (e.g., "Đà Lạt", "Hà Nội").
                - durationDays (int): number of days (default 1 if not mentioned).
                - tags (List<String>): preferences keywords.
                
                IMPORTANT: Return ONLY raw JSON. Do not use markdown formatting.
                """;

            String fullPrompt = systemInstruction + "\nUser Input: " + prompt;

            // Call FLASH model for speed and cost-efficiency
            String jsonResponse = geminiService.callGemini(fullPrompt, GeminiService.ModelType.FLASH);

            // Parse string to Java Object
            return objectMapper.readValue(cleanJson(jsonResponse), ParsedIntent.class);

        } catch (Exception e) {
            log.error("Error parsing intent: ", e);
            // Case: AI returns wrong format or network error
            throw new ApiException(ErrorCode.AI_RESPONSE_FORMAT_ERROR, "Error parsing user intent: " + e.getMessage());
        }
    }

    private String generateItinerary(String userPrompt, ParsedIntent intent, List<PoiData> pois) {
        try {
            // Convert Java POI List to JSON string to embed in prompt
            String poiJsonContext = objectMapper.writeValueAsString(pois);
            String currentDate = LocalDate.now().toString();

            // Composite Prompt
            String finalPrompt = String.format("""
                [ROLE] You are an expert AI Travel Planner for TravelEZ system.
                
                [CONTEXT]
                1. Current Date: %s
                2. User Request: "%s"
                3. Trip Duration: %d days
                4. APPROVED POI LIST (Source of Truth - Only choose from here): 
                %s
                
                [TASK]
                Create a detailed itinerary based on the User Request and Approved POIs.
                - Strictly select POIs from the provided list. Do NOT hallucinate new places.
                - Optimize route logic (Morning -> Afternoon -> Evening).
                - IMPORTANT: You MUST include the exact 'id' from the source list as 'poi_id' in the output.
                
                [OUTPUT FORMAT]
                Return strict JSON with structure:
                {
                  "trip_title": "string",
                  "summary": "string",
                  "schedule": [
                    { 
                      "day": 1, 
                      "activities": [ 
                        { 
                          "time": "HH:mm", 
                          "poi_id": "string",
                          "poi_name": "string", 
                          "description": "string" 
                        } 
                      ] 
                    }
                  ]
                }
                Return ONLY JSON. No markdown block.
                """, currentDate, userPrompt, intent.getDurationDays(), poiJsonContext);

            // Call PRO model for complex logic processing
            String response = geminiService.callGemini(finalPrompt, GeminiService.ModelType.PRO);

            return cleanJson(response);

        } catch (Exception e) {
            log.error("Error generating itinerary: ", e);
            throw new ApiException(ErrorCode.AI_PROCESSING_ERROR, "Error generating itinerary: " + e.getMessage());
        }
    }

    /**
     * Utility method to clean JSON string from AI response
     * (Remove ```json ... ``` blocks if present)
     */
    private String cleanJson(String text) {
        if (text == null || text.isEmpty()) return "{}";

        // 1. Xóa Markdown block code ```json ... ```
        text = text.replaceAll("```json", "").replaceAll("```", "").trim();

        // 2. Cắt lấy đúng phần JSON object (từ '{' đến '}')
        int firstBrace = text.indexOf("{");
        int lastBrace = text.lastIndexOf("}");

        if (firstBrace != -1 && lastBrace != -1 && firstBrace < lastBrace) {
            return text.substring(firstBrace, lastBrace + 1);
        }

        return text; // Trả về nguyên gốc nếu không tìm thấy cấu trúc JSON
    }
}