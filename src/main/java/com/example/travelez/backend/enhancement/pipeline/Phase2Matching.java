package com.example.travelez.backend.enhancement.pipeline;

import com.example.travelez.backend.enhancement.model.ItineraryEnhancementContext;
import com.example.travelez.backend.infrastructure.gemini.GeminiEmbeddingService;
import com.example.travelez.backend.poi.model.Poi;
import com.example.travelez.backend.poi.repository.PoiRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class Phase2Matching {
    private final PoiRepository poiRepository;
    private final GeminiEmbeddingService geminiEmbeddingService;

    public void execute(ItineraryEnhancementContext context) {
        log.info("Phase 2: Starting data matching (Vector Matching)...");
        List<String> rawNames = context.getRawPoiNames();

        if (rawNames == null || rawNames.isEmpty()) {
            log.info("Phase 2: No locations to match.");
            return;
        }

        List<float[]> vectors = geminiEmbeddingService.embedTexts(rawNames);

        for (int i = 0; i < rawNames.size(); i++) {
            String rawName = rawNames.get(i);

            if (i >= vectors.size() || vectors.get(i) == null) {
                context.getUnknownPoiNames().add(rawName);
                continue;
            }

            String vectorStr = Arrays.toString(vectors.get(i));

            List<Poi> top5Candidates = poiRepository.findClosestPoisByVector(vectorStr, 5);

            Poi matchedPoi = null;

            for (Poi candidate : top5Candidates) {
                if (isNameMatched(rawName, candidate.getName())) {
                    matchedPoi = candidate;
                    break;
                }
            }

            if (matchedPoi != null) {
                long targetId = matchedPoi.getId();
                boolean isAlreadyAdded = context.getKnownPois().stream()
                        .anyMatch(p -> p.getId() == targetId);

                if (!isAlreadyAdded) {
                    context.getKnownPois().add(matchedPoi);
                }
                log.info("Phase 2 - KNOWN_POI: Predicted '{}' -> Matched '{}'", rawName, matchedPoi.getName());
            } else {
                context.getUnknownPoiNames().add(rawName);
                log.info("Phase 2 - UNKNOWN_POI: No reliable match found for '{}'", rawName);
            }
        }

        log.info("Phase 2 Completed: Successfully identified {} KNOWN POIs, detected {} UNKNOWN POIs.",
                context.getKnownPois().size(), context.getUnknownPoiNames().size());
    }

    private boolean isNameMatched(String rawName, String dbName) {
        String normalizeRaw = rawName.toLowerCase().replaceAll("[^\\p{L}\\p{N}]", "");
        String normalizeDb = dbName.toLowerCase().replaceAll("[^\\p{L}\\p{N}]", "");

        if (normalizeRaw.isBlank() || normalizeDb.isBlank()) return false;

        return normalizeDb.contains(normalizeRaw) || normalizeRaw.contains(normalizeDb);
    }
}
