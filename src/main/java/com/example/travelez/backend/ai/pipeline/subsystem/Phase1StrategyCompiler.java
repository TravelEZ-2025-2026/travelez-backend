package com.example.travelez.backend.ai.pipeline.subsystem;

import com.example.travelez.backend.ai.pipeline.model.SemanticQueryMap;
import com.example.travelez.backend.common.api.ResultCode;
import com.example.travelez.backend.common.exception.ApiException;
import com.example.travelez.backend.infrastructure.gemini.GeminiService;
import com.example.travelez.backend.itinerary.dto.request.ItineraryCreationRequest;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class Phase1StrategyCompiler {
    private final GeminiService geminiService;
    private final Gson gson;

    public SemanticQueryMap generateSearchQueries(ItineraryCreationRequest request) {
        log.info("--- [PHASE 1] Generating semantic search queries ---");

        String requestJsonStr = gson.toJson(request);

        String prompt = buildPrompt(requestJsonStr);

        try {
            log.debug("Calling Gemini for Semantic Queries...");
            String jsonResponse = geminiService.generateJson(prompt, GeminiService.ModelType.FLASH_LITE);

            SemanticQueryMap result = gson.fromJson(jsonResponse, SemanticQueryMap.class);

            return result;

        } catch (Exception e) {
            log.error("Phase 1 failed to generate search queries.", e);
            throw new ApiException(ResultCode.AI_SERVICE_ERROR, "The AI model is currently overloaded and unable to respond.");
        }
    }

    private String buildPrompt(String requestJsonStr) {
        return """
                You are an expert at writing ENGLISH semantic search queries for travel POI retrieval.

                Input user request JSON (may contain Vietnamese; do NOT translate it in your reply):
                %s

                Your job is to generate short, high-signal English search queries for embedding retrieval.
                The goal is to retrieve places that match the user's real intent.

                CRITICAL DATABASE CONTEXT:
                The POI database embeddings are constructed strictly from these fields, joined together:
                "Category: [Category Name]. Vibe: [text]. Scenario: [text]. Audience: [text]. Price Perception: [text]. Additional_Info: Amenities: [list] | Highlights: [text]."
                The database embeddings DO NOT contain POI names.

                Return ONLY a JSON object (no markdown) with this schema:
                {
                  "search_queries": {
                    "ATTRACTION": "<English search phrase>",
                    "NATURE": "<English search phrase>",
                    "RESTAURANT": "<English search phrase>",
                    "CAFE_DESSERT": "<English search phrase>",
                    "SHOPPING": "<English search phrase>",
                    "RELIGIOUS": "<English search phrase>",
                    "NIGHTLIFE": "<English search phrase>",
                    "STREET_FOOD": "<English search phrase>",
                    "OTHER": "<English search phrase>"
                  }
                }

                Instructions:
                1) Every query in "search_queries" MUST be English.
                2) DO NOT use proper nouns, POI names, district names, neighborhood names, or street names. The database relies on descriptive semantic traits, not names.
                3) Construct queries using words and phrases that strongly overlap with the database fields: Category, Vibe, Scenario, Audience, Amenities, and Highlights.
                4) Prefer concise, natural descriptive phrases that reflect those fields, rather than generic travel keywords or awkward keyword lists.
                5) Good queries should combine as many relevant signals as possible, such as:
                   - vibe or atmosphere
                   - scenario or use-case
                   - audience suitability
                   - practical comfort / amenities
                   - distinctive highlights
                6) Avoid empty generic adjectives and weak phrases such as "best", "famous", "must-visit", "good atmosphere", or "nice place".
                7) The highest-priority signal is the user's "specialNotes" and deeper personal intent.
                8) If a category is not genuinely suitable for this user, return an empty string "". It is better to leave a category empty than to force a weak query.
                9) Be especially strict with CAFE_DESSERT: only generate a query if cafes are clearly relevant to the user's intent, or if a cafe would provide meaningful comfort, rest, atmosphere, or pacing value.
                10) Keep each query concise but descriptive, ideally 8 to 20 words.
                11) VECTOR MATCHING HINT (CRITICAL): Because the database vectors start with the "Category:" prefix, you MUST prepend the target category to your search queries to maximize retrieval accuracy.
                    - Example for RESTAURANT: "Category: Restaurant. Luxury fine dining for anniversary..."
                    - Example for OTHER: "Category: Spa. Private wellness and massage..."
                12) CATEGORY MAPPING FOR NICHE INTENTS: If the user requests Spa, Wellness, Workshops, or private tours, strictly emphasize these in the "OTHER" category query. If they request Fine-dining or Michelin, explicitly write "luxury fine dining" in the "RESTAURANT" query.
                13) CONTEXT TRANSLATION (CRITICAL): If the user has physical, geographical, or logistical constraints (e.g., transit, carrying luggage, mobility issues, or wanting to be near a specific hub like an airport/station), you MUST explicitly translate these into practical keywords that would appear in a review's 'Amenities' or 'Highlights' (e.g., "located near the airport", "spacious for luggage", "wheelchair accessible", "quick service").

                Examples of strong query style:
                - "Category: Restaurant. relaxed romantic rooftop dinner for couples with city lights and elegant evening vibe"
                - "Category: Street Food. lively local street food setting for casual evening eating with energetic atmosphere"
                - "Category: Attraction. quiet indoor family-friendly place with short walking and comfortable air-conditioned stop"
                - "Category: Nature. scenic shaded riverside atmosphere for slow walking and relaxed daytime break"
                - "Category: Other. spacious indoor area located near the airport, convenient for short transit, easy to move with luggage, quick service"

                Optimize strictly for semantic vector matching against the described database format.
                Output only valid JSON.
                """.formatted(requestJsonStr);
    }
}
