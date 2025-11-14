package com.example.travelez.backend.common.exception;

import com.example.travelez.backend.common.api.IErrorCode;
import lombok.Getter;

@Getter
public class ApiException extends RuntimeException {

    private IErrorCode errorCode;

    public ApiException(IErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    // Default is INTERNAL_SERVER_ERROR
    public ApiException(String message) {
        super(message);
    }

    public ApiException(IErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ApiException(Throwable cause) {
        super(cause);
    }

    public ApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
