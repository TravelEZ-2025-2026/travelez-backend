package com.example.travelez.backend.common.exception;

import com.example.travelez.backend.common.api.ApiResponse;
import com.example.travelez.backend.common.api.ResultCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(value = ApiException.class)
    public ResponseEntity<ApiResponse> handleApiException(ApiException e) {
        if (e.getErrorCode() != null) {
            return ResponseEntity.status(e.getErrorCode().getCode())
                    .body(ApiResponse.failed(e.getErrorCode(), e.getMessage(), null));
        }
        return ResponseEntity.status(ResultCode.INTERNAL_SERVER_ERROR.getCode())
                .body(ApiResponse.failed(ResultCode.INTERNAL_SERVER_ERROR, e.getMessage(), null));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Object>> handleBadCredentialsException(BadCredentialsException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.failed(ResultCode.UNAUTHORIZED, e.getMessage(), null));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Object>> handleAccessDeniedException(AccessDeniedException e) {
        return ResponseEntity.status(ResultCode.FORBIDDEN.getCode())
                .body(ApiResponse.failed(ResultCode.FORBIDDEN, e.getMessage(), null));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        return ResponseEntity.status(ResultCode.VALIDATION_FAILED.getCode())
                .body(ApiResponse.<Map<String, String>>failed(ResultCode.VALIDATION_FAILED, ex.getMessage(), errors));
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(BindException ex) {

        BindingResult bindingResult = ex.getBindingResult();
        Map<String, String> errors = new HashMap<>();
        bindingResult.getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        return ResponseEntity.status(ResultCode.VALIDATION_FAILED.getCode())
                .body(ApiResponse.<Map<String, String>>failed(ResultCode.VALIDATION_FAILED, ex.getMessage(), errors));
    }

}
