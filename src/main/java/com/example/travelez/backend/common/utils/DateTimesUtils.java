package com.example.travelez.backend.common.utils;

import java.time.LocalDateTime;

public class DateTimesUtils {

    public static final LocalDateTime MIN_EPOCH_TIME = LocalDateTime.of(1970, 1, 1, 0, 0);

    private DateTimesUtils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
