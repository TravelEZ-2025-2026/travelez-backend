package com.example.travelez.backend.common.api;

import org.springframework.http.ResponseEntity;

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

    public static <T> ResponseEntity<ApiResponse<T>> success(T data, ResultCode resultCode) {
        return ResponseEntity.status(resultCode.getCode())
                .body(new ApiResponse<T>(resultCode.getCode(), resultCode.getMessage(), data, true));
    }

    public static <T> ResponseEntity<ApiResponse<T>> success(T data, ResultCode resultCode, String message) {
        return ResponseEntity.status(resultCode.getCode())
                .body(new ApiResponse<T>(resultCode.getCode(), message == "" ? resultCode.getMessage() : message, data,
                        true));
    }

    public static <T> ResponseEntity<ApiResponse<T>> failed(T data, IErrorCode errorCode) {
        return ResponseEntity.status(errorCode.getCode())
                .body(new ApiResponse<T>(errorCode.getCode(), errorCode.getMessage(), data, false));
    }

    public static <T> ResponseEntity<ApiResponse<T>> failed(T data, IErrorCode errorCode, String message) {
        return ResponseEntity.status(errorCode.getCode())
                .body(new ApiResponse<T>(errorCode.getCode(), message == "" ? errorCode.getMessage() : message, data,
                        false));
    }
}
