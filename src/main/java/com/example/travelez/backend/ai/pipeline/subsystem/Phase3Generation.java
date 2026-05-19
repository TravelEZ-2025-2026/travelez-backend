package com.example.travelez.backend.ai.pipeline.subsystem;

import com.example.travelez.backend.ai.pipeline.model.PoiVectorResult;
import com.example.travelez.backend.itinerary.dto.request.ItineraryReplanRequest;
import com.example.travelez.backend.ai.pipeline.model.PipelineContext;
import com.example.travelez.backend.common.api.ResultCode;
import com.example.travelez.backend.common.exception.ApiException;
import com.example.travelez.backend.infrastructure.gemini.GeminiService;
import com.example.travelez.backend.itinerary.dto.request.ItineraryCreationRequest;
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
        List<PoiVectorResult> poiPool = context.getRetrievedPois();

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
                    map.put("poi_type", p.getPoiType() != null ? p.getPoiType() : "OTHER");
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
        tripContext.put("start_date", reqData.getStartDate().toString());
        tripContext.put("end_date", reqData.getEndDate().toString());

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
            throw new ApiException(ResultCode.AI_SERVICE_ERROR, "The AI model is currently overloaded and unable to respond.");
        }
    }

    private String buildPrompt(long numDays, String tripContextJson, String poisJson, boolean hasKids) {
        int preferredMin = 3;
        int preferredMax = hasKids ? 4 : 6;

        return """
            You are generating the FIRST DRAFT of a multi-day travel itinerary.
            
            User trip context:
            %s
            
            CANDIDATE POIS (CRITICAL INSTRUCTION - ALREADY PRE-SORTED):
            The POIs below have been mathematically sorted by our Vector AI. The items at the TOP of this list are the STRONGEST matches for the user's personal semantic profile.
            Strongly prioritize selecting POIs from the top of the list unless routing, opening hours, or category limits strictly forbid it:
            %s
            
            PRE-STEP (MANDATORY INTERNAL REASONING - DO NOT OUTPUT):
                - Read the user's `specialNotes`, `styles`, and explicit dates (`start_date` to `end_date`) carefully.
                - USE GOOGLE SEARCH TOOL to actively find out:
                  + Overall weather behavior during these specific dates in {destination_cities}.
                  + Special events, local festivals, night markets, or seasonal phenomena happening precisely during this date range.
                - For EACH candidate POI, Mentally assess how well its `semantic_text` and current seasonality matches the user's core intent. Classify them into Tiers (1, 2, 3).
            
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
            - INCORPORATE REAL-TIME SEARCH DATA: If you found special events or seasonal highlights via search during their trip dates, gracefully mention them in `reasoningSummary` or `aiTip` (e.g. "Vì bạn đi vào dịp [Sự kiện X], mình đã ưu tiên...", "Thời tiết lúc này thường [thời tiết], hãy nhớ mang theo ô").
            - LANGUAGE RULE: You MUST write the `tripTitle`, `reasoningSummary`, and `aiTip` in the EXACT SAME LANGUAGE used in the user's `specialNotes`. If `specialNotes` is empty, null, or generic, you MUST write them in VIETNAMESE.
            - `reasoningSummary`: Act as a professional, welcoming Travel Advisor. Write an engaging paragraph explaining how this trip captures their specific travel style. DO NOT mention technical logic, tiers, budget math, or routing constraints.
            - `aiTip`: Act as an expert local tour guide. Read the `description` of the POI and provide 1-2 sentences of highly specific, actionable advice (e.g., signature dish, specific photo angle, what to look out for). STRICTLY FORBIDDEN: Do not use generic filler phrases like "Great place for photos".

            === FINAL OUTPUT INSTRUCTION (STRICT JSON ONLY) ===
            You MUST return ONLY a fully valid JSON object. 
            Do NOT include conversational text before or after the JSON.
            Do NOT wrap the JSON in markdown blocks (e.g. no ``` or ```json).
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
                        "id": 12345,    // MUST map to exact poi_id from Candidate POIs
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
            """.formatted(
                tripContextJson,
                poisJson,
                preferredMin,
                preferredMax,
                hasKids,
                numDays
        );
    }

    public String generateReplanItinerary(PipelineContext context, ItineraryReplanRequest replanReq) {
        log.info("--- [PHASE 3] Generating REPLAN itinerary ---");

        if (context.getRetrievedPois() == null || context.getRetrievedPois().isEmpty()) {
            throw new ApiException(ResultCode.AI_SERVICE_ERROR, "Not enough POIs retrieved for replan.");
        }

        List<Map<String, Object>> poiListForPrompt = context.getRetrievedPois().stream().map(p -> {
            Map<String, Object> map = new HashMap<>();
            map.put("poi_id", p.getId());
            map.put("name", p.getName());
            map.put("poi_type", p.getPoiType() != null ? p.getPoiType() : "OTHER");
            map.put("semantic_text", p.getSemanticText() != null ? p.getSemanticText() : "");
            return map;
        }).collect(Collectors.toList());

        Map<String, Object> tripContext = new HashMap<>();
        tripContext.put("destination_cities", replanReq.getDestinationCities());
        tripContext.put("styles", replanReq.getStyles());
        tripContext.put("companion", replanReq.getCompanion());
        tripContext.put("hasKids", replanReq.getHasKids());
        tripContext.put("specialNotes", replanReq.getSpecialNotes());
        tripContext.put("budget_total_vnd", replanReq.getBudget());
        tripContext.put("start_date", replanReq.getStartDate().toString());
        tripContext.put("end_date", replanReq.getEndDate().toString());

        long numDays = ChronoUnit.DAYS.between(replanReq.getStartDate(), replanReq.getEndDate()) + 1;
        long startDayOfWeek = replanReq.getStartDate().getDayOfWeek().getValue();

        tripContext.put("numDays", numDays);
        tripContext.put("start_day_of_week", startDayOfWeek);

        // -- REPLAN CONTEXT --
        tripContext.put("feedback_notes", replanReq.getFeedbackNotes());
        tripContext.put("previous_itinerary", replanReq.getPreviousItinerary());

        tripContext.put("rejected_poi_ids", replanReq.getRejectedPoiIds() != null ? replanReq.getRejectedPoiIds() : List.of());

        String prompt = buildReplanPrompt(tripContext, poiListForPrompt);

        try {
            String rawJsonResponse = geminiService.generateJson(prompt, GeminiService.ModelType.FLASH_LITE);
            rawJsonResponse = cleanJsonResponse(rawJsonResponse);

            log.info("Phase 3 REPLAN generated JSON successfully.");
            return rawJsonResponse;
        } catch (Exception e) {
            log.error("Phase 3 REPLAN Generation failed: ", e);
            throw new ApiException(ResultCode.AI_SERVICE_ERROR, "The AI model is currently overloaded and unable to respond.");
        }
    }

    /**
     * PROMPT DÀNH CHO REPLAN: Sử dụng Text Blocks và gson có sẵn
     */
    private String buildReplanPrompt(Map<String, Object> tripContext, List<Map<String, Object>> candidatePois) {
        String previousItineraryJson = gson.toJson(tripContext.get("previous_itinerary"));
        String candidatePoisJson = gson.toJson(candidatePois);
        String rejectedIdsJson = gson.toJson(tripContext.get("rejected_poi_ids"));

        return """
            You are an expert AI Travel Planner explicitly REVISING an existing multi-day travel itinerary.
            
            TRIP OVERVIEW:
            - Style: %s
            - Companion: %s (Kids: %s)
            - Total days: %s (Starts on actual date: %s)
            - Budget estimate target: %s VND
            - Additional requirements: %s

            === REPLAN CONTEXT (CRITICAL) ===
            The user was NOT completely satisfied with the previous itinerary and provided this feedback:
            "%s"

            Here is the PREVIOUS ITINERARY for your reference:
            %s

            EXPLICITLY REJECTED POI IDs:
            %s
            These specific POI IDs were rejected by the user. You are STRICTLY FORBIDDEN from including them in the new JSON output.

            CANDIDATE POIS TO CHOOSE FROM (CRITICAL INSTRUCTION - ALREADY FILTERED & SORTED):
            These POIs have been mathematically sorted by our Vector AI. The items at the TOP are the best matching alternatives based on the user's profile.
            Strongly prioritize top POIs unless you are replacing them with something that better addresses the user's Replan feedback:
            %s

            YOUR REVISION TASK:
            1. Generate a COMPLETELY NEW valid JSON itinerary that incorporates the user's feedback.
            2. Analyze what needs to change from the previous itinerary and logically swap routing/activities.
            3. Do not suggest any rejected POI IDs. Choose alternative replacements from the Candidate POIs.
            4. Make sure your adjusted schedule still strictly complies with realistic opening hours, OSRM distances, and travel time concepts.
            ==================================

            1. LOGISTICS & TIME RULES:
            - The schedule must be realistic and spaced efficiently across %s days.
            - Start day around 08:00 or 08:30. End the day around 21:00 to 22:00.
            - Assign specific `id` of POIs from the Candidate list exactly.
            - Set reasonable `startTime` and `endTime` in "HH:mm" format (e.g. "08:00").
            - A restaurant POI should ideally span 1 to 1.5 hours. Activities usually span 1.5 to 3 hours.

            2. USER EXPERIENCE & CONTENT:
            - Mention HOW you resolved the user's feedback right in the `reasoningSummary` (in VIETNAMESE or exact language of user's notes).
            - Write personalized and highly specific `aiTip` based on semantic_text for each selected activity.

            === FINAL OUTPUT INSTRUCTION (STRICT JSON ONLY) ===
            You MUST return ONLY a fully valid JSON object.
            Do NOT include conversational text before or after the JSON.
            Do NOT wrap the JSON in markdown blocks (e.g. no ``` or ```json).
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
            """.formatted(
                tripContext.get("styles"),
                tripContext.get("companion"),
                tripContext.get("hasKids"),
                tripContext.get("numDays"),
                tripContext.get("start_date"),
                tripContext.get("budget_total_vnd"),
                tripContext.get("specialNotes"),
                tripContext.get("feedback_notes"),
                previousItineraryJson,
                rejectedIdsJson,
                candidatePoisJson,
                tripContext.get("numDays")
        );
    }

    //--------------------------------------- Helper function-----------------------------------
    private String cleanJsonResponse(String raw) {
        if (raw == null || raw.isBlank()) return null;

        int start = -1;
        int max = raw.length();
        for (int i = 0; i < max; i++) {
            if (raw.charAt(i) == '{') {
                start = i;
                break;
            }
        }
        if (start == -1) return raw;

        int counter = 0;
        boolean inString = false;
        boolean escape = false;
        int end = -1;

        for (int i = start; i < max; i++) {
            char c = raw.charAt(i);
            if (escape) {
                escape = false;
            } else if (c == '\\') {
                escape = true;
            } else if (c == '"') {
                inString = !inString;
            } else if (!inString) {
                if (c == '{') counter++;
                else if (c == '}') counter--;

                if (counter == 0) {
                    end = i;
                    break;
                }
            }
        }

        if (end != -1) {
            return raw.substring(start, end + 1);
        }

        int lastEnd = raw.lastIndexOf('}');
        if (start < lastEnd) {
            return raw.substring(start, lastEnd + 1);
        }
        return raw;
    }
}
