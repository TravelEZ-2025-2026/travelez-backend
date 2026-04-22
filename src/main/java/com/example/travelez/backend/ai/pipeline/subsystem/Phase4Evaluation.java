package com.example.travelez.backend.ai.pipeline.subsystem;

import com.example.travelez.backend.ai.pipeline.model.EvaluationReport;
import com.example.travelez.backend.ai.pipeline.model.PipelineContext;
import com.example.travelez.backend.itinerary.dto.response.ItineraryResponse;
import com.example.travelez.backend.itinerary.dto.response.utils.ActivityDTO;
import com.example.travelez.backend.itinerary.dto.response.utils.DayPlan;
import com.example.travelez.backend.poi.model.Poi;
import com.example.travelez.backend.poi.model.PoiHour;
import com.example.travelez.backend.poi.repository.PoiHourRepository;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
@Slf4j
public class Phase4Evaluation {

    private final Gson gson;
    private final RestClient restClient;
    private final PoiHourRepository poiHourRepository;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final String OSRM_BASE_URL = "http://router.project-osrm.org/route/v1/driving";
    private static final double OSRM_FALLBACK_MINS = 15.0;
    private static final int LOGISTICS_EXTRA_BUFFER_MINS = 10;

    private final Map<String, Double> osrmCache = new ConcurrentHashMap<>();

    // Constructor Injection
    public Phase4Evaluation(Gson gson, PoiHourRepository poiHourRepository) {
        this.gson = gson;
        this.poiHourRepository = poiHourRepository;
        this.restClient = RestClient.builder().build();
    }

    public EvaluationReport evaluate(PipelineContext context) {
        log.info("--- [PHASE 4] Evaluating itinerary with OSRM Logistics, Budget & Hours ---");
        List<String> errors = new ArrayList<>();
        String rawJson = context.getRawLlmResponse();

        if (rawJson == null || rawJson.isBlank()) {
            errors.add("LLM returned empty response.");
            return buildReport(errors);
        }

        ItineraryResponse parsedResponse;
        try {
            parsedResponse = gson.fromJson(rawJson, ItineraryResponse.class);
        } catch (Exception e) {
            log.error("Failed to parse JSON in Phase 4: {}", e.getMessage());
            errors.add("Invalid JSON format. Could not parse into ItineraryResponse object.");
            return buildReport(errors);
        }

        // Lấy danh sách POI gốc từ context
        Map<Long, Poi> poiMap = context.getRetrievedPois().stream()
                .collect(Collectors.toMap(Poi::getId, p -> p, (p1, p2) -> p1));

        Set<Long> globalPoiIds = new HashSet<>();

        // 2. Gom toàn bộ POI ID trong lịch trình lại
        if (parsedResponse.getDays() != null) {
            for (DayPlan day : parsedResponse.getDays()) {
                if (day.getActivities() != null) {
                    for (ActivityDTO act : day.getActivities()) {
                        globalPoiIds.add(act.getId());
                    }
                }
            }
        }

        // 3. Query DB lấy "poi_hours" của tất cả các điểm đó rồi Group lại theo POI_ID
        Map<Long, List<PoiHour>> poiHoursMap = new HashMap<>();
        if (!globalPoiIds.isEmpty()) {
            List<PoiHour> dbHours = poiHourRepository.findByPoiIdIn(globalPoiIds);
            poiHoursMap = dbHours.stream().collect(Collectors.groupingBy(PoiHour::getPoiId));
        }

        // ---------------------------------------------------------
        // EVAL 1: BUDGET
        // ---------------------------------------------------------
        BigDecimal userBudget = context.getOriginalRequest().getBudget();
        BigDecimal totalEstimated = calculateTotalCost(parsedResponse);
        if (userBudget != null && totalEstimated.compareTo(userBudget) > 0) {
            BigDecimal excess = totalEstimated.subtract(userBudget);
            errors.add("[BUDGET VIOLATION] Total cost is " + totalEstimated + ", exceeding budget of " + userBudget + " by " + excess + ".");
        }

        LocalDate tripStartDate = context.getOriginalRequest().getStartDate();
        Set<Long> duplicateCheckIds = new HashSet<>();

        for (DayPlan day : parsedResponse.getDays()) {
            List<ActivityDTO> activities = day.getActivities();
            if (activities == null || activities.isEmpty()) continue;

            LocalTime previousEndTime = null;
            Poi previousPoi = null;

            // Lấy ra Ngày và Tính thứ (dow). (VD: Thứ Hai = 2, CN = 8)
            LocalDate currentDate = tripStartDate.plusDays(day.getDayIndex() - 1);
            // DayOfWeek.getValue() bên java trả về 1=Thứ Hai..7=Chủ Nhật. Cộng thêm 1 để khớp cơ chế T2=2..CN=8.
            int currentDowNumber = currentDate.getDayOfWeek().getValue() + 1;

            for (int i = 0; i < activities.size(); i++) {
                ActivityDTO act = activities.get(i);
                Poi currentPoi = poiMap.get(act.getId());

                if (!duplicateCheckIds.add(act.getId())) {
                    errors.add("[DUPLICATE VIOLATION] POI ID " + act.getId() + " is duplicated in the itinerary.");
                }

                LocalTime currentStartTime = parseTimeGuard(act.getStartTime(), errors, day.getDayIndex(), act.getActivityName(), "startTime");
                LocalTime currentEndTime = parseTimeGuard(act.getEndTime(), errors, day.getDayIndex(), act.getActivityName(), "endTime");

                if (currentStartTime != null && currentEndTime != null) {
                    if (!currentStartTime.isBefore(currentEndTime)) {
                        errors.add("[HOURS VIOLATION] Day " + day.getDayIndex() + " - Activity '" + act.getActivityName() + "': Start time must be before end time.");
                    }

                    // ---------------------------------------------------------
                    // EVAL 2: OPERATING HOURS
                    // ---------------------------------------------------------
                    List<PoiHour> hoursList = poiHoursMap.getOrDefault(act.getId(), Collections.emptyList());
                    if (!hoursList.isEmpty()) {
                        // Tìm giờ mở cửa khớp với cột day_of_week trong DB
                        hoursList.stream()
                                .filter(oh -> oh.getDayOfWeek() != null && oh.getDayOfWeek() == currentDowNumber)
                                .findFirst()
                                .ifPresentOrElse(oh -> {
                                    LocalTime dbOpen = oh.getOpenTime();
                                    LocalTime dbClose = oh.getCloseTime();

                                    // Xử lý nếu opening/close ghi 00:00 (mở xuyên ngày)
                                    if (!(dbOpen.equals(LocalTime.MIDNIGHT) && dbClose.equals(LocalTime.MIDNIGHT))) {
                                        if (currentStartTime.isBefore(dbOpen) || currentEndTime.isAfter(dbClose)) {
                                            errors.add(String.format(
                                                    "[HOURS VIOLATION] Day %d: %s scheduled %s - %s but open %s - %s",
                                                    day.getDayIndex(), act.getActivityName(),
                                                    TIME_FORMATTER.format(currentStartTime), TIME_FORMATTER.format(currentEndTime),
                                                    TIME_FORMATTER.format(dbOpen), TIME_FORMATTER.format(dbClose)
                                            ));
                                        }
                                    }
                                }, () -> {
                                    // POI không có record đăng ký mở cửa thứ đó -> Mặc định ĐÓNG CỬA
                                    errors.add(String.format("[HOURS VIOLATION] Day %d: %s is CLOSED on Day-of-week %d.", day.getDayIndex(), act.getActivityName(), currentDowNumber));
                                });
                    }

                    // ---------------------------------------------------------
                    // EVAL 3: LOGISTICS (OSRM Check)
                    // ---------------------------------------------------------
                    if (previousEndTime != null && previousPoi != null && currentPoi != null) {
                        long bufferMinsLong = ChronoUnit.MINUTES.between(previousEndTime, currentStartTime);
                        if (currentStartTime.isBefore(previousEndTime)) {
                            bufferMinsLong = bufferMinsLong * (-1);
                        }

                        double bufferMins = (double) bufferMinsLong;
                        double travelMins = getOsrmDurationCached(previousPoi.getLatitude(), previousPoi.getLongitude(), currentPoi.getLatitude(), currentPoi.getLongitude());
                        double requiredMins = travelMins + LOGISTICS_EXTRA_BUFFER_MINS;

                        if (bufferMins < requiredMins) {
                            double shortBy = Math.round((requiredMins - bufferMins) * 100.0) / 100.0;
                            String overlapReason = (bufferMins < 0) ? "overlap_or_insufficient_travel_time" : "insufficient_travel_time";

                            errors.add(String.format(
                                    "[LOGISTICS VIOLATION] Day %d: %s (ends %s) -> %s (starts %s) | buffer=%.1f min, travel=%.1f min, required=%.1f min => SHORT by %.1f min. Reason: %s",
                                    day.getDayIndex(), previousPoi.getName(), TIME_FORMATTER.format(previousEndTime),
                                    currentPoi.getName(), TIME_FORMATTER.format(currentStartTime),
                                    bufferMins, travelMins, requiredMins, shortBy, overlapReason
                            ));
                        }
                    }
                    previousEndTime = currentEndTime;
                    previousPoi = currentPoi;
                }
            }
        }

        return buildReport(errors);
    }

    private LocalTime parseTimeGuard(String timeStr, List<String> errors, int dayIndex, String actName, String fieldName) {
        if (timeStr == null || timeStr.isBlank()) {
            errors.add("[HOURS VIOLATION] Day " + dayIndex + " - Activity '" + actName + "': " + fieldName + " is missing.");
            return null;
        }
        try {
            return LocalTime.parse(timeStr.trim(), TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            errors.add("[HOURS VIOLATION] Day " + dayIndex + " - Activity '" + actName + "': Invalid " + fieldName + " format ('" + timeStr + "').");
            return null;
        }
    }

    private BigDecimal calculateTotalCost(ItineraryResponse response) {
        BigDecimal total = BigDecimal.ZERO;
        if (response.getDays() != null) {
            for (DayPlan day : response.getDays()) {
                if (day.getActivities() != null) {
                    for (ActivityDTO act : day.getActivities()) {
                        if (act.getPrice() != null) total = total.add(act.getPrice());
                    }
                }
            }
        }
        return total;
    }

    // ---------------------------------------------------------
    // SỬ DỤNG RESTCLIENT
    // ---------------------------------------------------------
    private double getOsrmDurationCached(Double lat1, Double lon1, Double lat2, Double lon2) {
        if (lat1 == null || lon1 == null || lat2 == null || lon2 == null) return OSRM_FALLBACK_MINS;

        String key = String.format(Locale.US, "%.4f,%.4f;%.4f,%.4f", lon1, lat1, lon2, lat2);

        if (osrmCache.containsKey(key)) return osrmCache.get(key);

        String uri = OSRM_BASE_URL + "/" + key + "?overview=false";

        try {
            Map response = restClient.get()
                    .uri(uri)
                    .retrieve()
                    .body(Map.class);

            if (response != null && "Ok".equals(response.get("code"))) {
                List<Map<String, Object>> routes = (List<Map<String, Object>>) response.get("routes");
                if (routes != null && !routes.isEmpty()) {
                    double durationSec = Double.parseDouble(routes.get(0).get("duration").toString());
                    double durationMins = durationSec / 60.0;

                    osrmCache.put(key, durationMins);
                    Thread.sleep(50);
                    return durationMins;
                }
            }
        } catch (Exception e) {
            log.warn("OSRM RestClient failed for {}. Fallback {}. Msg: {}", key, OSRM_FALLBACK_MINS, e.getMessage());
        }

        osrmCache.put(key, OSRM_FALLBACK_MINS);
        return OSRM_FALLBACK_MINS;
    }

    private EvaluationReport buildReport(List<String> errors) {
        boolean passed = errors.isEmpty();
        if (!passed) {
            log.warn("Phase 4 FAILED: Found {} logic/logistics errors. Details:", errors.size());
            for (int i = 0; i < errors.size(); i++) {
                log.warn("   {}. {}", i + 1, errors.get(i));
            }
            // ---------------------------------------------
        } else {
            log.info("Phase 4 PASSED: No logic/logistics errors found.");
        }

        return EvaluationReport.builder()
                .isPassed(passed)
                .errors(errors)
                .build();
    }
}
