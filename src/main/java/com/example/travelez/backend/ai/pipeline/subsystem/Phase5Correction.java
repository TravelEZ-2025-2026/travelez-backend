package com.example.travelez.backend.ai.pipeline.subsystem;

import com.example.travelez.backend.ai.pipeline.model.EvaluationReport;
import com.example.travelez.backend.ai.pipeline.model.PipelineContext;
import com.example.travelez.backend.common.api.ResultCode;
import com.example.travelez.backend.common.exception.ApiException;
import com.example.travelez.backend.infrastructure.gemini.GeminiService;
import com.example.travelez.backend.itinerary.dto.request.ItineraryCreationRequest;
import com.example.travelez.backend.itinerary.dto.response.ItineraryResponse;
import com.example.travelez.backend.itinerary.dto.response.utils.ActivityDTO;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class Phase5Correction {

    private final GeminiService geminiService;
    private final Gson gson;

    public String fixItineraryWithLlm2(PipelineContext context, EvaluationReport report) {
        log.info("--- [PHASE 5] Correction with LLM2 ---");

        // 1. Build feedback text từ danh sách lỗi của Phase 4
        String feedbackText = String.join("\n", report.getErrors());
        log.debug("Feedback sent to LLM2:\n{}", feedbackText);

        // 2. Trích xuất danh sách các POI đã được sử dụng trong bản JSON lỗi
        String rawLlm1Response = context.getRawLlmResponse();
        ItineraryResponse parsedDraft = null;
        try {
            parsedDraft = gson.fromJson(rawLlm1Response, ItineraryResponse.class);
        } catch (Exception e) {
            throw new ApiException(ResultCode.AI_SERVICE_ERROR, "Unable to read LLM1 data to fix the error.");
        }

        Set<Long> usedPoiIds = parsedDraft.getDays().stream()
                .filter(day -> day.getActivities() != null)
                .flatMap(day -> day.getActivities().stream())
                .map(ActivityDTO::getId)
                .collect(Collectors.toSet());

        // Lấy thông tin chi tiết của các điểm đó để LLM2 tham chiếu
        List<Map<String, Object>> usedPoisInfo = context.getRetrievedPois().stream()
                .filter(p -> usedPoiIds.contains(p.getId()))
                .map(p -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("poi_id", p.getId());
                    m.put("name", p.getName());
                    m.put("poi_type", p.getPoiType() != null ? p.getPoiType() : "OTHER");
                    m.put("address", p.getAddress() != null ? p.getAddress() : "");
                    //m.put("semantic_text", p.getSemanticText() != null ? p.getSemanticText() : "");
                    m.put("description", p.getDescription() != null ? p.getDescription() : "");

                    if (p.getOpeningHour() != null) m.put("operating_hours", p.getOpeningHour());
                    return m;
                })
                .collect(Collectors.toList());

        // 3. Build Prompt
        ItineraryCreationRequest reqData = context.getOriginalRequest();
        String prompt = buildCorrectionPrompt(
                reqData.getHasKids(),
                reqData.getSpecialNotes(),
                rawLlm1Response,
                feedbackText,
                gson.toJson(usedPoisInfo)
        );

        // 4. Gọi LLM2
        try {
            log.info("Sending repair instructions to Gemini...");
            String fixedJson = geminiService.generateJson(prompt, GeminiService.ModelType.FLASH_LITE);
            fixedJson = cleanJsonResponse(fixedJson);

            log.info("Phase 5 Correction JSON generated successfully.");
            return fixedJson;
        } catch (Exception e) {
            log.error("Failed to repair itinerary in Phase 5", e);
            throw new ApiException(ResultCode.AI_SERVICE_ERROR, "Unable to fix AI error at this time.");
        }
    }

    private String buildCorrectionPrompt(boolean hasKids, String specialNotes, String originalJson, String feedbackText, String usedPoisJson) {

        String safeSpecialNotes = (specialNotes != null && !specialNotes.isBlank()) ? specialNotes : "EMPTY";

        return """
            You are repairing an existing multi-day travel itinerary.
            Return ONLY valid JSON (no markdown). Do not output any explanation.
            
            Your job is NOT to preserve every original stop. Your job is to produce the smallest necessary set of changes that makes the itinerary both mathematically valid and meaningfully better.
            
            1. THE "KILL SWITCH", DENSITY & FOOD REDUCTION RULE
            - DENSITY ENFORCEMENT: If `hasKids` is True (Current: %b), the day MUST NOT exceed 4 total activities (including meals). If the draft has more, YOU MUST DELETE the weakest/filler POIs until it reaches 4.
            - FOOD/DRINK REDUCTION: Target MAXIMUM 1 Cafe and 2 Restaurants per day.
            - PRIME SLOT PROTECTION: If a routine Restaurant or Cafe occupies the late afternoon/sunset slot (16:30-18:30) for a visual trip, REMOVE IT or shift it to dinner.
            
            2. LOGISTICS REPAIR MATH (CRITICAL)
            - Formula: `required_buffer_mins = osrm_travel_mins + 10.0`
            - To fix an overlap/short buffer, shift the `startTime` down so the gap is AT LEAST `required_buffer_mins`.
            - ROUND TIMES: ALL start/end times MUST end in exactly ":00" or ":30".
            
            3. INTENT PROTECTION & DEDUPLICATION
            - Ensure no `id` (POI ID) is repeated.
            
            4. TONE & USER EXPERIENCE REPAIR (STRICT)
            - LANGUAGE RULE: You MUST write the new `reasoningSummary` and `aiTip` in the EXACT SAME LANGUAGE as this specialNotes: "%s". If it says "EMPTY", use VIETNAMESE. Do not switch to English unless the specialNotes is in English.
            - REWRITE `reasoningSummary`: You MUST rewrite the summary to be a warm, passionate tour guide introduction tailored to the user's vibe. STRICTLY FORBIDDEN: You must NEVER mention the evaluator feedback, time adjustments, deleted duplicates, or budget fixes. Hide all technical repairs.
            - REWRITE `aiTip`: Ensure every `aiTip` is a highly specific, actionable insider tip based on the provided `semantic_text` (e.g., "Order the truffle pasta" or "Head to the 3rd-floor balcony for the best sunset angle"). Do not use generic filler words.
            
            Output schema must exactly follow:
            {
              "tripTitle": "...",
              "reasoningSummary": "Passionate, user-facing overview. NO DEBUG OR REPAIR LOGS.",
              "estimatedBudget": {
                   "total": 1000000,\s
                   "transportation": 200000,\s
                   "activity": 300000,\s
                   "foodAndDrink": 500000,\s
                   "accommodation": 0,\s
                   "currency": "VND"
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
                        "activityName": "...",
                        "price": 50000,
                        "aiTip": "Specific insider tip."
                      }
                   ]
                }
              ]
            }
            
            --- LLM1 Itinerary to repair ---
            %s
            
            --- Evaluator feedback (ALL violations to fix) ---
            %s
            
            --- POI Reference (Use semantic_text to write better aiTips) ---
            %s
            """.formatted(hasKids, safeSpecialNotes, originalJson, feedbackText, usedPoisJson);
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
