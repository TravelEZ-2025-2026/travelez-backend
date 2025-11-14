package com.example.travelez.backend.common.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiResponse<T> {
    private long code;
    private String message;
    private T data;
    private boolean success;

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<T>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), data, true);
    }

    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<T>(ResultCode.SUCCESS.getCode(), message, data, true);
    }

    public static <T> ApiResponse<T> failed(IErrorCode errorCode) {
        return new ApiResponse<T>(errorCode.getCode(), errorCode.getMessage(), null, false);
    }

    public static <T> ApiResponse<T> failed(IErrorCode errorCode, String message, T data) {
        return new ApiResponse<T>(errorCode.getCode(), message == "" ? errorCode.getMessage() : message, data, false);
    }
}
