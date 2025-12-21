package com.example.travelez.backend.ai.prompt;

import com.example.travelez.backend.common.api.ResultCode;
import com.example.travelez.backend.common.exception.ApiException;
import com.example.travelez.backend.itinerary.dto.request.ItineraryCreationRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@Slf4j
public class ItineraryPromptBuilder {
    private static final String PROMPT_TEMPLATE_PATH = "prompts/itinerary_prompt.txt";

    public String buildPrompt(ItineraryCreationRequest req, String poiContext) {
        try {
            String template = loadPromptTemplate();

            StringBuilder companionInfo = new StringBuilder(req.getCompanion() != null ? req.getCompanion() : "Solo");

            if (Boolean.TRUE.equals(req.getHasKids())) {
                companionInfo.append(" (Có trẻ em đi cùng - Cần an toàn)");
            }
            if (Boolean.TRUE.equals(req.getHasPets())) {
                companionInfo.append(" (Có mang theo thú cưng - Cần không gian mở)");
            }

            // 3. Xử lý Destination (List -> String)
            String destinations = req.getDestinationCities() != null ?
                    String.join(", ", req.getDestinationCities()) : "";

            return template.formatted(
                    poiContext,               // %s thứ 1: [POI CONTEXT]
                    destinations,             // %s thứ 2: Điểm đến
                    req.getStartDate(),       // %s thứ 3: Từ ngày
                    req.getEndDate(),         // %s thứ 4: Đến ngày
                    req.getBudget(),          // %s thứ 5: Ngân sách
                    req.getStyles(),          // %s thứ 6: Phong cách
                    companionInfo.toString(), // %s thứ 7: Đồng hành & Ràng buộc
                    req.getSpecialNotes()     // %s thứ 8: Ghi chú đặc biệt
            );

        } catch (IOException e) {
            log.error("Error loading prompt template from path: {}", PROMPT_TEMPLATE_PATH, e);
            throw new ApiException(ResultCode.INTERNAL_SERVER_ERROR,"Could not load itinerary prompt template");
        }
    }

    private String loadPromptTemplate() throws IOException {
        ClassPathResource resource = new ClassPathResource(PROMPT_TEMPLATE_PATH);
        return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
    }
}
