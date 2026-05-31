package com.example.travelez.backend.moderation.service.impl;

import com.example.travelez.backend.common.api.ResultCode;
import com.example.travelez.backend.common.exception.ApiException;
import com.example.travelez.backend.infrastructure.gemini.GeminiService;
import com.example.travelez.backend.moderation.dto.internal.AIModerationResponse;
import com.example.travelez.backend.moderation.service.AIModerationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AIModerationServiceImpl implements AIModerationService {

    private final GeminiService geminiService;
    private final ObjectMapper objectMapper;

    @Override
    @Retryable(
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2),
            retryFor = {Exception.class}
    )
    public AIModerationResponse analyzeContent(String text, List<String> imageUrls) {
        try {
            String prompt = buildPrompt(text, imageUrls);
            String jsonResponse = geminiService.generateJson(prompt, GeminiService.ModelType.FLASH);
            
            return parseResponse(jsonResponse);
        } catch (Exception e) {
            log.error("AI moderation failed: {}", e.getMessage(), e);
            throw new ApiException(ResultCode.AI_SERVICE_ERROR, "AI moderation failed: " + e.getMessage());
        }
    }

    private String buildPrompt(String text, List<String> imageUrls) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("""
                Bạn là một chuyên gia kiểm duyệt nội dung (Content Moderator) cấp cao cho nền tảng Mạng xã hội Du lịch Travelez tại Việt Nam.
                Nhiệm vụ của bạn là phân tích văn bản và danh sách hình ảnh được cung cấp nhằm phát hiện hành vi vi phạm pháp luật và tiêu chuẩn cộng đồng.
                
                Hãy phân tích toàn bộ ngữ cảnh dữ liệu dựa trên 5 bộ quy tắc nghiêm ngặt sau:
                1. CHINH_TRI: Chống nhà nước, bôi nhọ lãnh tụ, bàn luận chính trị nhạy cảm, bản đồ sai chủ quyền (thiếu Hoàng Sa, Trường Sa, có đường lưỡi bò).
                2. KHIEU_DAM: Hình ảnh khỏa thân, khiêu dâm, ngôn từ đồi trụy, kích dục, phản cảm.
                3. BAO_LUC: Máu me, kinh dị, kích động bạo lực, tự tử, ngược đãi.
                4. HANG_CAM: Quảng cáo cá độ, đánh bạc (tài xỉu, bet), ma túy, vũ khí, chất cấm.
                5. NGON_TU_THU_HET: Chửi thề tục tĩu, xúc phạm lăng mạ cá nhân, phân biệt vùng miền, tin giả (fake news).
                
                YÊU CẦU ĐẦU RA:
                Trả về kết quả DUY NHẤT dưới dạng chuỗi JSON chuẩn hóa.
                Cấu trúc định dạng JSON bắt buộc:
                {
                  "isSafe": boolean,
                  "violationType": "String",
                  "confidenceScore": number,
                  "reason": "String"
                }
                
                Trong đó:
                - isSafe: true nếu nội dung hoàn toàn sạch, false nếu vi phạm ít nhất 1 quy tắc.
                - violationType: Điền 1 trong các giá trị: "CHINH_TRI", "KHIEU_DAM", "BAO_LUC", "HANG_CAM", "NGON_TU_THU_HET". Nếu isSafe là true, bắt buộc điền "NONE".
                - confidenceScore: Điểm số từ 0.0 đến 1.0 thể hiện độ chính xác tự tin của AI.
                - reason: Giải thích ngắn gọn lý do vi phạm bằng tiếng Việt (Nếu isSafe là true, để chuỗi rỗng "").
                
                NỘI DUNG CẦN KIỂM DUYỆT:
                """);
        
        prompt.append("\nVăn bản: ").append(text != null ? text : "");
        
        if (imageUrls != null && !imageUrls.isEmpty()) {
            prompt.append("\n\nHình ảnh đính kèm: ").append(imageUrls.size()).append(" ảnh");
            // prompt.append("\n(Lưu ý: Chỉ phân tích văn bản trong phiên bản này, hình ảnh sẽ được kiểm duyệt sau)");
        }
        
        return prompt.toString();
    }

    private AIModerationResponse parseResponse(String jsonResponse) {
        try {
            return objectMapper.readValue(jsonResponse, AIModerationResponse.class);
        } catch (Exception e) {
            log.error("Failed to parse AI response: {}", jsonResponse, e);
            throw new ApiException(ResultCode.AI_SERVICE_ERROR, "Failed to parse AI response");
        }
    }
}
