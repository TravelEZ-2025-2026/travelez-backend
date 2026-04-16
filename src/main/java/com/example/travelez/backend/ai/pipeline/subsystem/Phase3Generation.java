package com.example.travelez.backend.ai.pipeline.subsystem;

import com.example.travelez.backend.ai.pipeline.model.PipelineContext;
import com.example.travelez.backend.common.api.ResultCode;
import com.example.travelez.backend.common.exception.ApiException;
import com.example.travelez.backend.infrastructure.gemini.GeminiService;
import com.example.travelez.backend.itinerary.dto.request.ItineraryCreationRequest;
import com.example.travelez.backend.poi.model.Poi;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class Phase3Generation {
    private final GeminiService geminiService;
    private final Gson gson;

    public String generateItinerary(PipelineContext context) {
        log.info("--- [PHASE 3] Generating itinerary (pure LLM) ---");

        ItineraryCreationRequest reqData = context.getOriginalRequest();
        List<Poi> poiPool = context.getRetrievedPois();

        if (poiPool == null || poiPool.isEmpty()) {
            throw new ApiException(ResultCode.VALIDATION_FAILED, "No suitable location found for this request.");
        }

        // 1. Tính toán số ngày
        long numDays = ChronoUnit.DAYS.between(reqData.getStartDate(), reqData.getEndDate()) + 1;

        // 2. Format POI data đưa vào prompt
        int maxPoisForPrompt = Math.min(poiPool.size(), 120);
        List<Map<String, Object>> poisForLlm = poiPool.stream()
                .limit(maxPoisForPrompt)
                .map(p -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("poi_id", p.getId());
                    map.put("name", p.getName());
                    map.put("poi_type", p.getPoiType() != null ? p.getPoiType().name() : "OTHER");
                    map.put("address", p.getAddress() != null ? p.getAddress() : "");
                    map.put("rating", p.getRating() != null ? p.getRating() : 3.0);
                    //map.put("semantic_text", p.getSemanticText() != null ? p.getSemanticText() : "");
                    map.put("description", p.getDescription() != null ? p.getDescription() : "");

                    if (p.getOpeningHour() != null) map.put("operating_hours", p.getOpeningHour());
                    return map;
                })
                .collect(Collectors.toList());

        // 3. Create context request cho Prompt
        Map<String, Object> tripContext = new HashMap<>();
        tripContext.put("destination_cities", reqData.getDestinationCities());
        tripContext.put("styles", reqData.getStyles());
        tripContext.put("companion", reqData.getCompanion());
        tripContext.put("hasKids", reqData.getHasKids());
        tripContext.put("specialNotes", reqData.getSpecialNotes());
        tripContext.put("budget_total_vnd", reqData.getBudget());

        long startDayOfWeek = reqData.getStartDate().getDayOfWeek().getValue(); // 1=Mon .. 7=Sun
        tripContext.put("start_day_of_week", startDayOfWeek);

        // 4. Sinh Prompt
        String prompt = buildPrompt(numDays, gson.toJson(tripContext), gson.toJson(poisForLlm), reqData.getHasKids());

        log.debug("Sending prompt to Gemini...");
        try {
            String rawJsonResponse = geminiService.generateJson(prompt, GeminiService.ModelType.FLASH_LITE);
            // THÊM BƯỚC KHỬ NHIỄU Ở ĐÂY:
            rawJsonResponse = cleanJsonResponse(rawJsonResponse);

            log.info("Phase 3 generated JSON successfully.");
            return rawJsonResponse;
        } catch (Exception e) {
            log.error("Failed to generate itinerary in Phase 3", e);
            throw new ApiException(ResultCode.AI_SERVICE_ERROR, "The AI cannot generate a schedule right now.");
        }
    }

    private String buildPrompt(long numDays, String tripContextJson, String poisJson, boolean hasKids) {
        int preferredMin = 3;
        int preferredMax = hasKids ? 4 : 6;

        return """
            You are generating the FIRST DRAFT of a multi-day travel itinerary.
            Return ONLY valid JSON (no markdown). Do not use an outer wrapper like 'itinerary_result'.
            
            PRE-STEP (MANDATORY INTERNAL REASONING - DO NOT OUTPUT):
            Read the user's `specialNotes`, `styles`, and context carefully.
            For EACH candidate POI, mentally assess how well its `semantic_text` matches the user's core intent.
            Classify them into Tiers: TIER 1 (Must-include/Strong match), TIER 2 (Good fit), TIER 3 (Filler).
            Prioritize TIER 1 and TIER 2. Use TIER 3 only if absolutely necessary (e.g., nearest meal).
            
            1. CORE PHILOSOPHY & PACING
            - QUALITY OVER QUANTITY: Create a coherent, intent-first itinerary. Do not pad the schedule with weak POIs just to fill time. A day with 2 amazing POIs + meals is better than a crammed day.
            - EMOTIONAL ARC: Day 1 (Arrival energy), Middle days (Core experiences), Last day (Grand finale or gentle wind-down). Maintain a consistent vibe within each day.
            - DENSITY: Target between %d to %d total activities per day (excluding meals).
            - RELAX/KIDS OVERRIDE: If `hasKids` is True (Current: %b) or `specialNotes` mentions 'relax'/'slow', enforce a STRICT MAXIMUM of 4 activities per day and ensure a rest gap after lunch.
            
            2. CATEGORY DISCIPLINE & STRICT LOGISTICS
            - DIVERSITY: Max 1 POI per category per day (except attractions/meals), to prevent category fatigue. Overide this ONLY if the user explicitly requested a themed tour in `specialNotes`.
            - NOISY LABEL DEFENSE: If a POI's name contains "Cafe", "Coffee", "Dessert", or "Bistro", strictly treat it as a CAFE.
            - MEALS & HARD LIMITS: Max 1 Cafe stop and 2 Restaurant stops per day. NEVER schedule consecutive F&B stops.
              * BREAKFAST: 07:00 - 10:00 | LUNCH: 11:30 - 14:00 | DINNER: 17:30 - 21:00
            - MIDDAY SHIELD: Between 11:30 - 14:30, prioritize indoor activities or long lunches to avoid heat.
            - PRIME VISUAL SLOT: The pre-sunset slot (16:30 - 18:30) is SACRED for visual/outdoor POIs. Do NOT schedule a Restaurant or Cafe here.
            - Exactly %d days. Activities MUST be sequential with NO overlaps.
            - ROUND TIMES: ALL start and end times MUST end in exactly ":00" or ":30" (e.g., 08:00, 08:30). Use practical travel buffers (15-45 mins).
            - STRICT NO DUPLICATION: Never use the same `poi_id` more than once across the entire trip.
            
            3. TONE & USER EXPERIENCE (CRITICAL)
            - LANGUAGE RULE: You MUST write the `tripTitle`, `reasoningSummary`, and `aiTip` in the EXACT SAME LANGUAGE used in the user's `specialNotes`. If `specialNotes` is empty, null, or generic, you MUST write them in VIETNAMESE.
            - `reasoningSummary`: Act as a professional, welcoming Travel Advisor. Write an engaging paragraph explaining how this trip captures their specific travel style. DO NOT mention technical logic, tiers, budget math, or routing constraints.
            - `aiTip`: Act as an expert local tour guide. Read the `semantic_text` of the POI and provide 1-2 sentences of highly specific, actionable advice (e.g., signature dish, specific photo angle, what to look out for). STRICTLY FORBIDDEN: Do not use generic filler phrases like "Great place for photos".

            Output schema must exactly follow:
            {
              "tripTitle": "Catchy naming for this trip",
              "reasoningSummary": "Short explanation why these places suit the user",
              "estimatedBudget": {
                 "total": 0, "transportation": 0, "activity": 0, "foodAndDrink": 0, "accommodation": 0, "currency": "VND"
              },
              "days": [
                {
                   "dayIndex": 1,
                   "date": "YYYY-MM-DD",
                   "activities": [
                      {
                        "id": 12345,
                        "startTime": "08:00",
                        "endTime": "09:30",
                        "activityName": "Short descriptive activity name",
                        "price": 50000,
                        "aiTip": "Practical and lively advice (2-3 sentences)..."
                      }
                   ]
                }
              ]
            }
            Note: `id` in activities MUST be the exact integer `poi_id` from Candidate POIs.
            
            User trip context:
            %s
            
            Candidate POIs (use address/hours/semantic_text to plan):
            %s
            """.formatted(
                preferredMin,
                preferredMax,
                hasKids,
                numDays,
                tripContextJson,
                poisJson
        );
    }

    private String cleanJsonResponse(String raw) {
        if (raw == null) return null;
        int start = raw.indexOf('{');
        int end = raw.lastIndexOf('}');
        if (start != -1 && end != -1 && start < end) {
            return raw.substring(start, end + 1);
        }
        return raw;
    }
}
