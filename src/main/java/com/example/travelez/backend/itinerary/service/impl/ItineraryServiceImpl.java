package com.example.travelez.backend.itinerary.service.impl;

import com.example.travelez.backend.common.exception.ApiException;
import com.example.travelez.backend.common.exception.Asserts;
import com.example.travelez.backend.common.exception.ErrorCode;
import com.example.travelez.backend.common.service.GeminiService;
import com.example.travelez.backend.itinerary.dto.request.CreateItineraryRequest;
import com.example.travelez.backend.itinerary.dto.request.SaveItineraryRequest;
import com.example.travelez.backend.itinerary.dto.response.ItineraryResponse;
import com.example.travelez.backend.itinerary.model.Itinerary;
import com.example.travelez.backend.itinerary.model.ItineraryActivity;
import com.example.travelez.backend.itinerary.repository.ItineraryActivityRepository;
import com.example.travelez.backend.itinerary.repository.ItineraryRepository;
import com.example.travelez.backend.itinerary.service.ItineraryService;
import com.example.travelez.backend.poi.model.Poi;
import com.example.travelez.backend.poi.repository.PoiRepository;
import com.example.travelez.backend.poi.service.PoiService;
import com.example.travelez.backend.users.model.User;
import com.example.travelez.backend.users.repository.UserRepository;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItineraryServiceImpl implements ItineraryService {
    private final GeminiService geminiService;
    private final PoiService poiService;
    private final Gson gson = new Gson();

    private final UserRepository userRepository;
    private final ItineraryRepository itineraryRepository;
    private final ItineraryActivityRepository itineraryActivityRepository;
    private final PoiRepository poiRepository;

    @Override
    public ItineraryResponse generateSmartItinerary(CreateItineraryRequest request) {
        // 1. Lấy dữ liệu Context (Real Data)
        List<Poi> contextPois = poiService.getActivePoisByCity(request.getDestinationCity());
        log.info("Generating itinerary with context of {} POIs", contextPois.size());

        // 2. Serialize POI để nạp vào Prompt (Rút gọn để tiết kiệm token)
        String poiContextJson = serializePois(contextPois);

        // 3. Build Prompt (Hybrid Structured)
        String prompt = buildPrompt(request, poiContextJson);

        // 4. Gọi AI (Model Flash)
        String jsonResult = geminiService.generateJson(prompt, GeminiService.ModelType.FLASH);

        // BƯỚC 5: HẬU XỬ LÝ & PARSE (Post-processing)
        try {
            // A. Clean JSON String bằng Regex trước khi Parse
            String cleanJson = jsonResult.replaceAll("\\s*[\\(\\[](?i)(?:ID\\s*)?\\d+[\\)\\]]", "");

            // B. Parse sang Object
            return gson.fromJson(cleanJson, ItineraryResponse.class);

        } catch (Exception e) {
            log.error("Failed to parse AI Response: {}", jsonResult);
            Asserts.fail(ErrorCode.AI_RESPONSE_FORMAT_ERROR);
            return null;
        }
    }

    @Override
    @Transactional
    public Long saveItinerary(SaveItineraryRequest request) {

        // 1. LẤY USER TỪ SECURITY CONTEXT (Chuẩn Spring Security)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ApiException(ErrorCode.UNAUTHORIZED, "Người dùng chưa đăng nhập");
        }

        String currentUsername = authentication.getName(); // Lấy username từ token đã giải mã

        // 2. TÌM ENTITY USER TRONG DB
        User traveler = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Không tìm thấy thông tin người dùng: " + currentUsername));

        ItineraryResponse aiData = request.getAiResult();
        if (aiData == null) {
            throw new ApiException(ErrorCode.VALIDATION_FAILED, "Dữ liệu lộ trình từ AI không được để trống");
        }

        // 3. TẠO ITINERARY HEADER
        Itinerary itinerary = Itinerary.builder()
                .traveler(traveler) // Gán User thật vào
                .title(aiData.getTripTitle())
                .type(request.getStyles() != null ? String.join(", ", request.getStyles()) : "General")
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .budget(request.getBudget())
                .objectives(aiData.getReasoningSummary())
                .status(Itinerary.ItineraryStatus.PLANNING)
                .build();

        Itinerary savedItinerary = itineraryRepository.save(itinerary);

        // 4. TẠO ACTIVITIES
        List<ItineraryActivity> activities = new ArrayList<>();

        if (aiData.getDays() != null) {
            for (ItineraryResponse.DayPlan day : aiData.getDays()) {
                LocalDate currentDate = LocalDate.parse(day.getDate());

                for (ItineraryResponse.Activity aiActivity : day.getActivities()) {

                    LocalTime startTime = parseStartTime(aiActivity.getTimeSlot());

                    Poi linkedPoi = null;
                    // Chỉ tìm nếu ID hợp lệ (> 0)
                    if (aiActivity.getLocationId() > 0) {
                        linkedPoi = poiRepository.findById(aiActivity.getLocationId())
                                .orElse(null);
                    }

                    ItineraryActivity activity = ItineraryActivity.builder()
                            .itinerary(savedItinerary)
                            .itineraryDate(currentDate)
                            .startTime(startTime)
                            .timeOfDay(calculateTimeOfDay(startTime))
                            .type(aiActivity.getActivityType())
                            .description(aiActivity.getActivityName())
                            .note(aiActivity.getNotes())
                            .poi(linkedPoi)
                            .build();

                    activities.add(activity);
                }
            }
        }
        itineraryActivityRepository.saveAll(activities);
        return savedItinerary.getId();
    }

    @Override
    @Transactional(readOnly = true)
    public ItineraryResponse getItineraryDetail(Long itineraryId) {
        // 1. Tìm Itinerary cha
        Itinerary itinerary = itineraryRepository.findById(itineraryId)
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Không tìm thấy lộ trình"));

        // 2. Query Activities (Đảm bảo đã Join Fetch POI trong Repository để tránh lỗi N+1 query)
        List<ItineraryActivity> dbActivities = itineraryActivityRepository
                .findByItineraryIdOrderByItineraryDateAscStartTimeAsc(itineraryId);

        // 3. Tái tạo cấu trúc JSON
        List<ItineraryResponse.DayPlan> days = new ArrayList<>();

        if (!dbActivities.isEmpty()) {
            // Group theo ngày
            Map<LocalDate, List<ItineraryActivity>> grouped = dbActivities.stream()
                    .collect(Collectors.groupingBy(
                            ItineraryActivity::getItineraryDate,
                            LinkedHashMap::new,
                            Collectors.toList()
                    ));

            int dayIndex = 1;
            for (Map.Entry<LocalDate, List<ItineraryActivity>> entry : grouped.entrySet()) {
                List<ItineraryResponse.Activity> activityDTOs = new ArrayList<>();

                for (ItineraryActivity act : entry.getValue()) {
                    ItineraryResponse.Activity dto = new ItineraryResponse.Activity();

                    // A. TimeSlot: Lấy giờ HH:mm
                    if (act.getStartTime() != null) {
                        dto.setTimeSlot(act.getStartTime().toString().substring(0, 5));
                    } else {
                        dto.setTimeSlot("");
                    }

                    // B. Xử lý Tên Hoạt động (Lấy từ phần đầu của description)
                    // Format lúc save: "Tên Hoạt Động|||Tên Địa Điểm"
                    String rawDesc = act.getDescription() != null ? act.getDescription() : "";
                    String[] parts = rawDesc.split("\\|\\|\\|");
                    dto.setActivityName(parts[0]); // Phần 1 là tên hoạt động (VD: "Tham quan Dinh Độc Lập")

                    // C. Xử lý Địa điểm (QUAN TRỌNG: Ưu tiên POI thật)
                    if (act.getPoi() != null) {
                        // Có liên kết POI -> Lấy ID và Name chuẩn từ bảng poi
                        dto.setLocationId(act.getPoi().getId());
                        dto.setLocationName(act.getPoi().getName());
                    } else {
                        // Không có POI (Địa điểm ảo/Custom) -> Lấy ID=0 và Name từ description
                        dto.setLocationId(0L);
                        dto.setLocationName(parts.length > 1 ? parts[1] : "");
                    }

                    dto.setActivityType(act.getType());
                    dto.setNotes(act.getNote());

                    activityDTOs.add(dto);
                }

                ItineraryResponse.DayPlan dayPlan = new ItineraryResponse.DayPlan();
                dayPlan.setDayIndex(dayIndex++);
                dayPlan.setDate(entry.getKey().toString());
                dayPlan.setTheme("Ngày " + (dayIndex - 1));
                dayPlan.setActivities(activityDTOs);

                days.add(dayPlan);
            }
        }

        ItineraryResponse response = new ItineraryResponse();
        response.setTripTitle(itinerary.getTitle());
        response.setReasoningSummary(itinerary.getObjectives());
        response.setDays(days);

        return response;
    }

    // --- HELPER METHODS ---

    // DTO nội bộ để rút gọn dữ liệu gửi cho AI
    private record SimplePoi(long id, String name, String type, String address, Double lat, Double lng, Object hours) {}

    private String serializePois(List<Poi> pois) {
        List<SimplePoi> simpleList = pois.stream().map(p -> new SimplePoi(
                p.getId(),
                p.getName(),
                p.getPoiType().toString(),
                p.getAddress(),
                p.getLatitude(),
                p.getLongitude(),
                p.getOpeningHour()
        )).collect(Collectors.toList());
        return gson.toJson(simpleList);
    }

    private String buildPrompt(CreateItineraryRequest req, String poiContext) {
        StringBuilder companionInfo = new StringBuilder(req.getCompanion() != null ? req.getCompanion() : "Solo");
        if (Boolean.TRUE.equals(req.getHasKids())) {
            companionInfo.append(" (Có trẻ em đi cùng - Cần an toàn)");
        }
        if (Boolean.TRUE.equals(req.getHasPets())) {
            companionInfo.append(" (Có mang theo thú cưng - Cần không gian mở)");
        }
        return """
            ### 1. VAI TRÒ & LUẬT CẤM (ROLE & CONSTRAINTS)
            Bạn là TravelEZ Expert - một hướng dẫn viên du lịch địa phương cực kỳ am hiểu, nhiệt tình và tâm lý.
            Nhiệm vụ: Thiết kế lịch trình du lịch tối ưu, truyền cảm hứng nhưng đảm bảo sức khỏe cho du khách (Khoa học hành vi).
            
            LUẬT CẤM & RÀNG BUỘC (CRITICAL RULES):
            1. TRUNG THỰC: Chỉ được chọn địa điểm có trong [POI CONTEXT]. KHÔNG Hallucination (bịa ID).
            2. LOGIC CƠ BẢN: Không xếp lịch vào giờ địa điểm đóng cửa.
            3. FORMAT VĂN BẢN: Tuyệt đối KHÔNG viết ID (ví dụ: '(ID 123)') vào trong nội dung văn bản.
            4. VĂN PHONG (Tone of Voice):
                + Nhiệt tình, thân thiện, khơi gợi cảm hứng khám phá.
                + Cụ thể hóa hành động (Action-oriented): Thay vì nói "Tham quan chợ", hãy nói "Thử trả giá khi mua quà lưu niệm và nếm thử chè Sài Gòn".
                + Tuyệt đối KHÔNG giải thích các vấn đề kỹ thuật (như "Do thiếu dữ liệu...", "Vì thuật toán...").
                + Tập trung vào GIÁ TRỊ TRẢI NGHIỆM của khách.
            
            5. QUẢN LÝ NĂNG LƯỢNG & NHỊP ĐỘ (ENERGY BALANCING):
                + Tự động phân loại ngầm: Các điểm `NATURE`, `ATTRACTION` (leo trèo, đi bộ) là **HIGH ENERGY** (Tốn sức). Các điểm `CAFE`, `FOOD`, `SHOPPING` là **LOW ENERGY** (Phục hồi).
                + Nguyên tắc Xen kẽ (Interleaving): KHÔNG xếp 2 địa điểm Tốn sức (High) đi liền nhau. Hãy chèn 1 điểm Phục hồi (Low) vào giữa.
                + Quy tắc "Dead Rest" (Tránh nắng): Khung giờ 12:00 - 14:00 BẮT BUỘC phải là hoạt động trong nhà, ăn uống hoặc nghỉ ngơi. Tuyệt đối không xếp hoạt động ngoài trời giờ này.
            
            6. YÊU CẦU LƯU TRÚ (ACCOMMODATION LOGISTICS):
                + Trong danh sách [POI CONTEXT] có bao gồm các địa điểm lưu trú (Khách sạn/Homestay/Resort).
                + Hãy chọn ra 1 địa điểm lưu trú phù hợp nhất với [Ngân sách] và [Phong cách] của khách để làm nơi nghỉ ngơi xuyên suốt chuyến đi.
                + Vào Ngày 1: Phải có hoạt động "Check-in/Nhận phòng" tại địa điểm này (Ưu tiên khoảng 14:00 hoặc khi mới đến).
                + Cuối mỗi ngày: Phải có hoạt động về lại địa điểm lưu trú này để nghỉ ngơi.
            7. RÀNG BUỘC ĐỐI TƯỢNG (COMPANION CONSTRAINTS):
                + Nếu có **Trẻ em**: TUYỆT ĐỐI KHÔNG xếp lịch đi Bar/Pub/Nightlife/Khu đèn đỏ.
                + Nếu có **Thú cưng**: BẮT BUỘC chọn địa điểm không gian mở, pet-friendly. TRÁNH bảo tàng/di tích nghiêm ngặt.
            
            ### 2. NGỮ CẢNH (CONTEXT)
            [POI CONTEXT]:
            %s
            
            [USER REQUEST]:
            - Điểm đến: %s
            - Thời gian: %s đến %s
            - Ngân sách: %s
            - Phong cách: %s
            - Đồng hành: %s
            - Ghi chú: %s
            
            ### 3. TÁC VỤ (TASK - HYBRID STRUCTURED)
            Bước 1: Suy nghĩ ngầm (Think silently).
            - Phân loại năng lượng (High/Low) cho các địa điểm.
            - Tìm kiếm trong Context một Khách sạn/Homestay tốt nhất để làm "Base" (Căn cứ).
            - Sắp xếp theo mô hình "Sóng hồi phục": Mệt -> Nghỉ -> Mệt -> Nghỉ.
            - Tính toán khoảng cách di chuyển để gom cụm địa lý quanh điểm lưu trú đã chọn.
            
            Bước 2: Xuất dữ liệu JSON.
            - Trả về kết quả khớp chính xác với cấu trúc JSON sau:
            {
              "tripTitle": "Tên chuyến đi thật kêu và hấp dẫn (Tiếng Việt)",
              "reasoningSummary": "Đoạn văn ngắn (3-4 câu) giải thích tại sao lịch trình này lại phù hợp với người dùng như là về phong cách, điều kiện, sở thích,....",
              "days": [
                {
                  "dayIndex": 1,
                  "date": "YYYY-MM-DD",
                  "theme": "Chủ đề trải nghiệm trong ngày",
                  "activities": [
                    { 
                        "timeSlot": "HH:MM", (Chỉ cần đưa ra GIỜ BẮT ĐẦU (Start Time), không cần giờ kết thúc.)
                        "locationId": <ID_INTEGER_FROM_CONTEXT>,
                        "locationName": "Tên địa điểm (Lấy chính xác từ Context)", 
                        "activityName": "Tên hoạt động", 
                        "activityType": "...", 
                        "notes": "Lời khuyên thực tế và sinh động (2-3 câu). Gợi ý cụ thể: Nên chụp ảnh góc nào? Món nào 'must-try'? Nên đi đứng/ăn mặc ra sao? (Viết tự nhiên, không chứa ID)" 
                    }
                  ]
                }
              ]
            }
            """.formatted(
                poiContext,
                req.getDestinationCity(),
                req.getStartDate(), req.getEndDate(),
                req.getBudgetLevel(),
                req.getStyles(),
                companionInfo.toString(),
                req.getSpecialNotes()
        );
    }

    private LocalTime parseStartTime(String timeString) {
        try {
            if (timeString == null) return null;
            // Phòng hờ AI vẫn trả về "09:00 - 10:00", ta chỉ lấy phần trước dấu gạch
            String cleanTime = timeString.split("-")[0].trim();
            return LocalTime.parse(cleanTime); // Format HH:mm
        } catch (Exception e) {
            return null; // Hoặc LocalTime.of(8, 0) làm default
        }
    }

    private String calculateTimeOfDay(LocalTime time) {
        if (time == null) return "ANYTIME";
        int hour = time.getHour();
        if (hour >= 5 && hour < 12) return "MORNING";
        if (hour >= 12 && hour < 18) return "AFTERNOON";
        return "EVENING";
    }
}