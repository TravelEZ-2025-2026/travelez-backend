package com.example.travelez.backend.infrastructure.gemini;

import com.example.travelez.backend.common.api.ResultCode;
import com.example.travelez.backend.common.exception.Asserts;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.GoogleSearch;
import com.google.genai.types.Tool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeminiService {
    private final Client client;

    public enum ModelType {
        PRO("gemini-2.5-pro"),
        FLASH_LITE("gemini-3.1-flash-lite-preview"),
        FLASH("gemini-2.5-flash");

        public final String modelName;

        ModelType(String modelName) {
            this.modelName = modelName;
        }
    }

    /**
     * 1. BASIC METHOD: Dùng cho các tác vụ Text thông thường (Zero-shot, Chat).
     */
    public String generateText(String promptText, ModelType modelType) {
        return callGeminiInternal(promptText, modelType, null);
    }

    /**
     * 2. STRUCTURED JSON METHOD: Dùng cho Module 1, 6, 7.
     * Tự động ép kiểu output về JSON để tránh lỗi format.
     */
    public String generateJson(String promptText, ModelType modelType) {
        try {
            // Cấu hình ép kiểu JSON (Native Schema Enforcement)
            GenerateContentConfig config = GenerateContentConfig.builder()
                    .responseMimeType("application/json")
                    .temperature(0.2f)
                    .build();

            GenerateContentResponse response = client.models.generateContent(
                    modelType.modelName,
                    promptText,
                    config
            );

            if (response == null || response.text() == null) {
                Asserts.fail(ResultCode.AI_SERVICE_ERROR, "Empty response from Gemini");
            }

            return response.text();

        } catch (Exception e) {
            log.error("Gemini API Error: ", e);
            throw new com.example.travelez.backend.common.exception.ApiException(
                    ResultCode.AI_SERVICE_ERROR, "Error calling AI Provider: " + e.getMessage()
            );
        }
    }

    /**
     * 3. MULTIMODAL METHOD: Dùng cho Module 5 (Kiểm duyệt Video/Ảnh).
     * (sẽ mở rộng khi làm tới Module 5)
     */
    // public String generateMultimodal(...) { ... }

    // --- INTERNAL HELPER (Hàm lõi xử lý gọi API) ---
    private String callGeminiInternal(String prompt, ModelType model, GenerateContentConfig config) {
        try {
            if (config == null) {
                config = GenerateContentConfig.builder()
                        .temperature(0.4f)
                        .build();
            }

            GenerateContentResponse response = client.models.generateContent(
                    model.modelName,
                    prompt,
                    config
            );

            if (response != null && response.text() != null) {
                return response.text();
            } else {
                return "{\"error\": \"Empty response from Gemini\"}";
            }

        } catch (Exception e) {
            log.error("Error calling Gemini API: {}", e.getMessage());
            return "{\"error\": \"Gemini API Error: " + e.getMessage() + "\"}";
        }
    }

    public String generateJsonWithSearch(String promptText, ModelType modelType) {
        try {
            Tool googleSearchTool = Tool.builder()
                    .googleSearch(GoogleSearch.builder().build())
                    .build();

            GenerateContentConfig config = GenerateContentConfig.builder()
                    .responseMimeType("application/json")
                    .temperature(0.2f)
                    .tools(List.of(googleSearchTool)) // Kích hoạt Search Grounding
                    .build();

            GenerateContentResponse response = client.models.generateContent(
                    modelType.modelName,
                    promptText,
                    config
            );

            if (response == null || response.text() == null) {
                Asserts.fail(ResultCode.AI_SERVICE_ERROR, "Empty response from Gemini");
            }

            return response.text();

        } catch (Exception e) {
            log.error("Gemini API Error with Search Grounding: ", e);
            throw new com.example.travelez.backend.common.exception.ApiException(
                    ResultCode.AI_SERVICE_ERROR, "Error calling AI Provider: " + e.getMessage()
            );
        }
    }
}
