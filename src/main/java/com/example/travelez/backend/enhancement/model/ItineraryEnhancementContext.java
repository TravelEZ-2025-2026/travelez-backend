package com.example.travelez.backend.enhancement.model;

import com.example.travelez.backend.poi.model.Poi;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
public class ItineraryEnhancementContext {
    // Input
    private MultipartFile documentFile;
    private String providerPrompt;
    private String extractedTextFromFile;

    // Phase 1: Output
    private List<String> rawPoiNames = new ArrayList<>();

    // Phase 2: Output
    private List<Poi> knownPois = new ArrayList<>();
    private List<String> unknownPoiNames = new ArrayList<>();

    // Phase 3: Output
    private List<Map<String, Object>> knownPoiInsights = new ArrayList<>();
    private List<Map<String, Object>> recommendationPool = new ArrayList<>();

    // Phase 4: Output
    private String llmFinalJsonResponse;
}
