package com.example.travelez.backend.ai.pipeline.subsystem;

import com.example.travelez.backend.ai.pipeline.model.PipelineContext;
import com.example.travelez.backend.ai.pipeline.model.PoiVectorResult;
import com.example.travelez.backend.ai.pipeline.model.SemanticQueryMap;
import com.example.travelez.backend.infrastructure.gemini.GeminiEmbeddingService;
import com.example.travelez.backend.itinerary.dto.request.ItineraryReplanRequest;
import com.example.travelez.backend.poi.model.Poi;
import com.example.travelez.backend.poi.repository.PoiRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class Phase2VectorRetrieval {
    private final GeminiEmbeddingService embeddingService;
    private final PoiRepository poiRepository;

    public List<PoiVectorResult> retrieveMatchingPois(PipelineContext context) {
        log.info("--- [PHASE 2] Starting Vector Retrieval ---");

        SemanticQueryMap queryMap = context.getSearchQueries();
        Map<String, String> queries = queryMap != null ? queryMap.getSearchQueries() : null;

        if (queries == null || queries.isEmpty()) {
            log.warn("No queries generated from Phase 1.");
            return List.of();
        }

        List<String> categories = new ArrayList<>();
        List<String> textsToEmbed = new ArrayList<>();

        for (Map.Entry<String, String> entry : queries.entrySet()) {
            if (entry.getValue() != null && !entry.getValue().isBlank()) {
                categories.add(entry.getKey());
                textsToEmbed.add(entry.getValue());
            }
        }

        // 1. Lấy vector hàng loạt từ Gemini
        List<float[]> embeddings = embeddingService.embedTexts(textsToEmbed);

        // 2. Query DB theo từng Category để lấy Top-K (Mặc định lấy k=25 limits per category)
        List<PoiVectorResult> allBalancedResults = new ArrayList<>();
        int limitPerCategory = 25;

        // --- LẤY VECTOR PROFILE TỪ CONTEXT ---
        String userVector = context.getUserProfileVector();

        for (int i = 0; i < categories.size(); i++) {
            String category = categories.get(i);
            String vectorStr = Arrays.toString(embeddings.get(i));
            List<PoiVectorResult> categoryPois;

            // --- BẮT ĐẦU LOGIC RẼ NHÁNH: NORMAL RETRIEVAL vs RE-RANKING ---
            if (userVector == null || userVector.isBlank()) {
                log.debug("Guest or Cold-start user. Retrieving top {} for category: {}", limitPerCategory, category);
                // Dùng Native Query cũ
                categoryPois = poiRepository.findTopPoisByCategoryAndVector(category, vectorStr, limitPerCategory);
            } else {
                log.debug("Active user detected. Applying Re-ranking top {} for category: {}", limitPerCategory, category);
                // Dùng Native Query mới với CTE (Bạn nhớ đảm bảo file PoiRepository đã có hàm này nhé)
                categoryPois = poiRepository.findTopPoisByCategoryWithReRanking(category, vectorStr, userVector, limitPerCategory);
            }

            allBalancedResults.addAll(categoryPois);
        }

        log.info("Total balanced POIs retrieved: {}", allBalancedResults.size());
        return allBalancedResults;
    }

    public List<PoiVectorResult> retrieveForReplan(ItineraryReplanRequest request, PipelineContext context) {
        List<PoiVectorResult> candidates = retrieveMatchingPois(context); // Gọi lại hàm phía trên

        if (request.getRejectedPoiIds() != null && !request.getRejectedPoiIds().isEmpty()) {
            candidates = candidates.stream()
                    .filter(poi -> !request.getRejectedPoiIds().contains(poi.getId()))
                    .collect(Collectors.toList());
            log.info("Phase 2 (Replan): Filtered out {} rejected POIs.", request.getRejectedPoiIds().size());
        }

        return candidates;
    }
}
