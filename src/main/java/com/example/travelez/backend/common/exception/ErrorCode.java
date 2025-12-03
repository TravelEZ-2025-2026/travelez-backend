package com.example.travelez.backend.common.exception;

import com.example.travelez.backend.common.api.IErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode implements IErrorCode {
    SUCCESS(200, "Thành công"),
    FAILED(500, "Lỗi hệ thống"),
    INTERNAL_SERVER_ERROR(500, "Lỗi máy chủ nội bộ"),

    // --- AUTHENTICATION / AUTHORIZATION ---
    UNAUTHORIZED(401, "Chưa đăng nhập hoặc token hết hạn"),
    FORBIDDEN(403, "Không có quyền truy cập tài nguyên này"),

    // --- INPUT VALIDATION ---
    VALIDATION_FAILED(400, "Dữ liệu đầu vào không hợp lệ"),
    PARAM_ERROR(400, "Tham số không đúng"),
    RESOURCE_NOT_FOUND(404, "Không tìm thấy tài nguyên yêu cầu"),

    // --- AI / GEMINI MODULE SPECIFIC ---
    // Lỗi khi gọi sang Google Gemini (Mạng, Key, Quota...)
    AI_SERVICE_UNAVAILABLE(503, "Dịch vụ AI hiện không khả dụng, vui lòng thử lại sau"),

    // Lỗi khi AI không thể xử lý prompt (Prompt quá dài, vi phạm policy...)
    AI_PROCESSING_ERROR(422, "AI không thể xử lý yêu cầu này"),

    // Lỗi khi AI trả về JSON sai format (Hallucination)
    AI_RESPONSE_FORMAT_ERROR(500, "Lỗi định dạng dữ liệu từ AI"),

    // Lỗi logic nghiệp vụ: Không tìm thấy địa điểm phù hợp
    ITINERARY_GENERATION_FAILED(400, "Không tìm thấy địa điểm phù hợp với yêu cầu của bạn");

    private final int code;
    private final String message;

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
