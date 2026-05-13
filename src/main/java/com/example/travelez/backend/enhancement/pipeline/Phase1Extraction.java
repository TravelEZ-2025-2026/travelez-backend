package com.example.travelez.backend.enhancement.pipeline;

import com.example.travelez.backend.common.api.ResultCode;
import com.example.travelez.backend.common.exception.ApiException;
import com.example.travelez.backend.enhancement.model.ItineraryEnhancementContext;
import com.example.travelez.backend.infrastructure.gemini.GeminiService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Type;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class Phase1Extraction {
    private final GeminiService geminiService;
    private final Gson gson;

    public void execute(ItineraryEnhancementContext context) {
        log.info("Phase 1: Extracting itinerary file from Provider...");
        try {
            String extractedText = parseFileToText(context.getDocumentFile());
            context.setExtractedTextFromFile(extractedText);

            String jsonArrayResult = extractPoisUsingGemini(extractedText, context.getProviderPrompt());

            Type listType = new TypeToken<List<String>>() {}.getType();
            List<String> rawPoiNames = gson.fromJson(jsonArrayResult, listType);

            if (rawPoiNames == null || rawPoiNames.isEmpty()) {
                log.warn("Phase 1: LLM did not detect any locations in the provided document.");
            } else {
                log.info("Phase 1: Successfully extracted {} place names.", rawPoiNames.size());
            }

            context.setRawPoiNames(rawPoiNames != null ? rawPoiNames : List.of());

        } catch (ApiException apie) {
            throw apie;
        } catch (Exception e) {
            log.error("Failed to parse JSON from AI during POI extraction", e);
            throw new ApiException(ResultCode.INTERNAL_SERVER_ERROR, "Failed to parse JSON from AI during POI extraction: " + e.getMessage());
        }
    }

    private String parseFileToText(MultipartFile file) throws Exception {
        if (file == null || file.isEmpty()) {
            throw new ApiException(ResultCode.BAD_REQUEST, "Attached file is invalid or empty.");
        }

        Tika tika = new Tika();
        String content = tika.parseToString(file.getInputStream());

        if (content == null || content.isBlank()) {
            throw new ApiException(ResultCode.BAD_REQUEST, "No text found in the file (File is empty or contains scanned images without OCR enabled).");
        }
        return content;
    }

    private String extractPoisUsingGemini(String extractedText, String providerPrompt) {
        String prompt = String.format(
                "You are a professional travel itinerary analysis agent. " +
                "Based on the EXTRACTED ITINERARY (TEXT) below and the GOAL provided by the Provider, " +
                "find and list the NAMES of all locations (restaurants, destinations, accommodations...) that appear.\n\n" +
                "=== STRICT REQUIREMENTS ===\n" +
                "1. ONLY RETURN a JSON array of strings.\n" +
                "2. DO NOT EXPLAIN ANYTHING, provide no response other than the JSON array.\n" +
                "3. If there are no results, return [].\n" +
                "4. IMPORTANT: Keep the extracted location names in their original Vietnamese language.\n\n" +
                "=== EXTRACTED TEXT ===\n%s\n\n" +
                "=== GOAL (PROVIDER PROMPT) ===\n%s",
                extractedText, providerPrompt
        );

        return geminiService.generateJson(prompt, GeminiService.ModelType.FLASH);
    }
}
