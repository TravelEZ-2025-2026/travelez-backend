package com.example.travelez.backend.ai.pipeline.subsystem;

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

    public List<Poi> retrieveMatchingPois(SemanticQueryMap queryMap) {
        log.info("--- [PHASE 2] Starting Vector Retrieval ---");

        Map<String, String> queries = queryMap.getSearchQueries();
        if (queries == null || queries.isEmpty()) {
            log.warn("No queries generated from Phase 1.");
            return List.of();
        }

        // Tách keys (categories) và values (text cần embed)
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
        List<Poi> allBalancedResults = new ArrayList<>();
        int limitPerCategory = 25;

        for (int i = 0; i < categories.size(); i++) {
            String category = categories.get(i);
            float[] vector = embeddings.get(i);

            // Xoay mảng float thành string dạng "[0.12, 0.45, ...]" để map vào PostgreSQL native query
            String vectorStr = Arrays.toString(vector);

            log.debug("Retrieving top {} for category: {}", limitPerCategory, category);
            List<Poi> categoryPois = poiRepository.findTopPoisByCategoryAndVector(category, vectorStr, limitPerCategory);

            allBalancedResults.addAll(categoryPois);
        }

        log.info("Total balanced POIs retrieved: {}", allBalancedResults.size());
        return allBalancedResults;
    }

    public List<Poi> retrieveForReplan(ItineraryReplanRequest request, SemanticQueryMap queryMap) {
        List<Poi> candidates = retrieveMatchingPois(queryMap);

        if (request.getRejectedPoiIds() != null && !request.getRejectedPoiIds().isEmpty()) {
            candidates = candidates.stream()
                    .filter(poi -> !request.getRejectedPoiIds().contains(poi.getId()))
                    .collect(Collectors.toList());
            log.info("Phase 2 (Replan): Filtered out {} rejected POIs.", request.getRejectedPoiIds().size());
        }

        return candidates;
    }
}
