package com.example.travelez.backend.infrastructure.gemini;

import com.example.travelez.backend.common.api.ResultCode;
import com.example.travelez.backend.common.exception.ApiException;
import com.google.genai.Client;
import com.google.genai.types.EmbedContentConfig;
import com.google.genai.types.EmbedContentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeminiEmbeddingService {
    private final Client client;
    private static final String EMBEDDING_MODEL = "text-embedding-004";

    public List<float[]> embedTexts(List<String> texts) {
        if (texts == null || texts.isEmpty()) return List.of();

        try {
            EmbedContentConfig config = EmbedContentConfig.builder()
                    .taskType("RETRIEVAL_QUERY")
                    .outputDimensionality(768)
                    .build();

            List<float[]> embeddingsList = new ArrayList<>();

            for (String text : texts) {
                EmbedContentResponse response = client.models.embedContent(
                        "gemini-embedding-2-preview",
                        text,
                        config
                );

                if (response.embeddings().isPresent() && !response.embeddings().get().isEmpty()) {

                    List<Float> floatValues = response.embeddings().get().get(0).values()
                            .orElse(java.util.Collections.emptyList());

                    if (!floatValues.isEmpty()) {
                        float[] vectorArray = new float[floatValues.size()];
                        for (int i = 0; i < floatValues.size(); i++) {
                            vectorArray[i] = floatValues.get(i);
                        }
                        embeddingsList.add(vectorArray);
                    } else {
                        log.warn("Gemini returned empty embedding values for text: {}", text);
                        embeddingsList.add(new float[768]);
                    }
                } else {
                    log.warn("Gemini returned empty embedding array for text: {}", text);
                    embeddingsList.add(new float[768]);
                }
            }
            return embeddingsList;

        } catch (Exception e) {
            log.error("Failed to generate embeddings", e);
            throw new ApiException(ResultCode.AI_SERVICE_ERROR, "Error creating query vector");
        }
    }
}
