package com.example.travelez.backend.ai.pipeline.subsystem;

import com.example.travelez.backend.ai.pipeline.model.PipelineContext;
import com.example.travelez.backend.itinerary.dto.response.ItineraryResponse;
import com.example.travelez.backend.itinerary.dto.response.utils.ActivityDTO;
import com.example.travelez.backend.itinerary.dto.response.utils.DayPlan;
import com.example.travelez.backend.poi.model.Poi;
import com.example.travelez.backend.poi.repository.PoiRepository;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class Phase6Enrichment {

    private final PoiRepository poiRepository;
    private final Gson gson;

    public ItineraryResponse parseAndEnrich(PipelineContext context) {
        log.info("--- [PHASE 6] Parsing JSON and Enriching data ---");

        String rawJson = context.getRawLlmResponse();

        String finalJson = cleanRawJson(rawJson);

        ItineraryResponse response;
        try {
            // Bước 1: Dịch ngược kết quả văn bản thô của AI (đã qua Phase 3 hoặc Phase 5) thành Object
            response = gson.fromJson(finalJson, ItineraryResponse.class);
        } catch (Exception e) {
            log.error("Failed to parse Final LLM JSON: {}", finalJson, e);
            throw new RuntimeException("Could not parse AI generated itinerary into ItineraryResponse");
        }

        if (response == null || response.getDays() == null) {
            log.warn("Parsed response is null or has no days.");
            return response;
        }

        if (context.getOriginalRequest() != null) {
            response.setDestinationCities(context.getOriginalRequest().getDestinationCities());
        }

        // Bước 2: Liệt kê toàn bộ ID địa danh cần tham quan
        Set<Long> poiIds = new HashSet<>();
        for (DayPlan day : response.getDays()) {
            if (day.getActivities() != null) {
                for (ActivityDTO act : day.getActivities()) {
                    if (act.getId() > 0) {
                        poiIds.add(act.getId());
                    }
                }
            }
        }

        if (poiIds.isEmpty()) {
            return response;
        }

        // Bước 3: Lấy thông tin đầy đủ nhất từ Database
        List<Poi> pois = poiRepository.findAllById(poiIds);
        Map<Long, Poi> poiMap = pois.stream()
                .collect(Collectors.toMap(Poi::getId, p -> p));

        // Bước 4: Vá thông tin chuẩn xác từ Database đè lên dữ liệu ảo AI có thể sinh ra
        for (DayPlan day : response.getDays()) {
            if (day.getActivities() != null) {
                for (ActivityDTO act : day.getActivities()) {
                    Poi realPoi = poiMap.get(act.getId());
                    if (realPoi != null) {
                        // 1. Ghi đè tuyệt đối các thông tin từ DB để tránh AI bịa data
                        act.setTitle(realPoi.getName());
                        act.setAddress(realPoi.getAddress());
                        act.setLat(realPoi.getLatitude());
                        act.setLng(realPoi.getLongitude());

                        // 2. Ép cứng Type về chuẩn của DB
                        if (realPoi.getPoiType() != null) {
                            act.setActivityType(realPoi.getPoiType().name());
                        }

                        // Ưu tiên chọn ảnh đầu tiên làm Cover
                        if (realPoi.getMedias() != null && !realPoi.getMedias().isEmpty()) {
                            act.setImage(realPoi.getMedias().get(0).getUrl());
                        } else {
                            act.setImage(null);
                        }

                        // Giữ lại startTime, endTime, price, aiTip từ AI. Nếu giá null thì set 0.
                        if (act.getPrice() == null) {
                            act.setPrice(BigDecimal.ZERO);
                        }
                    } else { // Trường hợp ID AI trả về không tồn tại trong DB
                        if (act.getTitle() == null) {
                            act.setTitle("Hoạt động tự do");
                        }
                        act.setAddress("N/A");
                        act.setImage(null);
                        act.setActivityType("OTHER"); // Set một type an toàn mặc định
                        act.setLat(0.0);
                        act.setLng(0.0);
                        if (act.getPrice() == null) act.setPrice(BigDecimal.ZERO);
                    }
                }
            }
        }

        log.info("Phase 6 Enrichment completed. Enriched {} unique POIs.", poiIds.size());
        return response;
    }

    private String cleanRawJson(String rawLlmResponse) {
        if (rawLlmResponse == null || rawLlmResponse.isBlank()) {
            return "{}";
        }

        // 1. Cắt bỏ bọc Markdown ```json ... ``` nếu có
        String cleanStr = rawLlmResponse.trim();
        if (cleanStr.startsWith("```")) {
            cleanStr = cleanStr.replaceAll("^```(json)?|```$", "").trim();
        }

        // 2. Thuật toán lấy block JSON cân bằng dấu ngoặc (đề phòng đuôi } } ] dư thừa)
        int startIndex = cleanStr.indexOf('{');
        if (startIndex == -1) {
            return cleanStr; // Không tìm thấy object JSON
        }

        int balance = 0;
        int endIndex = -1;
        for (int i = startIndex; i < cleanStr.length(); i++) {
            char c = cleanStr.charAt(i);
            if (c == '{') {
                balance++;
            } else if (c == '}') {
                balance--;
                if (balance == 0) {
                    endIndex = i;
                    break; // Đã tìm thấy ngoặc đóng cân bằng của Object bao ngoài cùng
                }
            }
        }

        if (endIndex != -1) {
            // Chỉ lấy từ { đầu tiên đến } cân bằng cuối cùng
            return cleanStr.substring(startIndex, endIndex + 1);
        }

        return cleanStr;
    }
}