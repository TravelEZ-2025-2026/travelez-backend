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
    AI_SERVICE_ERROR(503, "Dịch vụ AI không phản hồi"),
    AI_RESPONSE_FORMAT_ERROR(500, "Lỗi định dạng dữ liệu từ AI"),

    // --- TRIP / ITINERARY ---
    DESTINATION_NOT_FOUND(404, "Không tìm thấy địa điểm du lịch yêu cầu"),
    NO_ACTIVE_POIS(400, "Không có địa điểm vui chơi nào khả dụng tại thành phố này"),
    ITINERARY_GENERATION_FAILED(500, "Không thể tạo lộ trình, vui lòng thử lại"),
    AI_SERVICE_UNAVAILABLE(503, "Hệ thống AI đang quá tải, vui lòng thử lại sau"),
    AI_PROCESSING_ERROR(422, "Không thể xử lý yêu cầu với AI");

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
