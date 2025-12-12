package com.example.travelez.backend.common.api;

import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;

@Getter
public enum ResultCode implements IErrorCode {
    // Success codes
    SUCCESS(HttpServletResponse.SC_OK, "Success"),
    CREATED(HttpServletResponse.SC_CREATED, "Created"),

    // Error codes
    UNAUTHORIZED(HttpServletResponse.SC_UNAUTHORIZED, "Authentication required."),
    FORBIDDEN(HttpServletResponse.SC_FORBIDDEN, "Access denied. You do not have permission to access this resource."),
    NOT_FOUND(HttpServletResponse.SC_NOT_FOUND, "Resource not found."),
    VALIDATION_FAILED(HttpServletResponse.SC_BAD_REQUEST, "Validation failed."),
    INTERNAL_SERVER_ERROR(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error."),
    CAPTCHA_INVALID(HttpServletResponse.SC_BAD_REQUEST, "Captcha verification failed."),
    AI_SERVICE_ERROR(503, "Dịch vụ AI không phản hồi"),
    AI_RESPONSE_FORMAT_ERROR(500, "Lỗi định dạng dữ liệu từ AI"),
    ;

    private final int code;
    private final String message;

    private ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
