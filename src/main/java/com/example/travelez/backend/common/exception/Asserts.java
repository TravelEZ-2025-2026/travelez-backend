package com.example.travelez.backend.common.exception;

import com.example.travelez.backend.common.api.IErrorCode;

public class Asserts {

    public static void fail(String message) {
        throw new ApiException(message);
    }

    public static void fail(IErrorCode errorCode) {
        throw new ApiException(errorCode);
    }

    public static void fail(IErrorCode errorCode, String message) {
        throw new ApiException(errorCode, message);
    }
}
