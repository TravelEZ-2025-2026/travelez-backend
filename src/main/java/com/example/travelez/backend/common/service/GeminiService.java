package com.example.travelez.backend.common.service;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GeminiService {
    private final Client client;

    // Enum chọn model (Bạn có thể mở rộng thêm)
    public enum ModelType {
        PRO("gemini-2.5-pro"),
        FLASH("gemini-2.5-flash"); // Hoặc gemini-2.5-flash nếu có quyền

        public final String modelName;

        ModelType(String modelName) {
            this.modelName = modelName;
        }
    }

    public String callGemini(String promptText, ModelType modelType){
        try{
            GenerateContentResponse response = client.models.generateContent(
                    modelType.modelName,
                    promptText,
                    null // Config thêm (temperature...) có thể để null nếu dùng mặc định
            );

            return response.text();
        }
        catch (Exception e){
            e.printStackTrace();
            return "Error calling Gemini API: " + e.getMessage();
        }
    }
}
