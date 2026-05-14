package com.example.travelez.backend.enhancement.pipeline;

import com.example.travelez.backend.enhancement.model.ItineraryEnhancementContext;
import com.example.travelez.backend.poi.model.Poi;
import com.example.travelez.backend.poi.repository.PoiRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class Phase3InsightPool {
    private final PoiRepository poiRepository;

    public void execute(ItineraryEnhancementContext context) {
        log.info("Phase 3: Initializing Insight and Recommendation Pool...");

        List<Long> knownPoiIds = context.getKnownPois().stream()
                .map(Poi::getId)
                .collect(Collectors.toList());

        if (!knownPoiIds.isEmpty()) {
            List<Object[]> insightResults = poiRepository.findInsightsByPoiIds(knownPoiIds);
            List<Map<String, Object>> mappedInsights = insightResults.stream().map(obj -> {
                Map<String, Object> map = new HashMap<>();
                map.put("poiName", obj[0]);
                map.put("insightType", obj[1]);
                map.put("evidence", obj[2] + " mentions");
                map.put("topKeywords", obj[3]);
                return map;
            }).collect(Collectors.toList());

            context.setKnownPoiInsights(mappedInsights);
            log.info("Phase 3 - Successfully retrieved insights for {} known POIs.", mappedInsights.size());
        }

        List<Object[]> poolResults = poiRepository.getRecommendationPool();
        List<Map<String, Object>> recommendationPool = poolResults.stream().map(obj -> {
            Map<String, Object> map = new HashMap<>();
            map.put("proposedPoi", obj[0]);
            map.put("targetGap", obj[1]);
            map.put("evidence", obj[2] + " mentions");
            map.put("description", obj[3]);
            return map;
        }).collect(Collectors.toList());

        context.setRecommendationPool(recommendationPool);
        log.info("Phase 3 - Successfully prepared Recommendation Pool with {} proposals.", recommendationPool.size());
    }
}
