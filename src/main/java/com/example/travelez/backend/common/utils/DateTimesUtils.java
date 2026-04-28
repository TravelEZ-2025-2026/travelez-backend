package com.example.travelez.backend.common.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class DateTimesUtils {

    public static final LocalDateTime MIN_EPOCH_TIME = LocalDateTime.of(1970, 1, 1, 0, 0);
    public static final LocalDateTime MAX_EPOCH_TIME = LocalDateTime.of(2099, 12, 31, 23, 59, 59);

    private DateTimesUtils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
    public static LocalDateTime getStartOfDay(LocalDate dateTime) {
        return dateTime == null ? MIN_EPOCH_TIME : dateTime.atStartOfDay();
    }

    public static LocalDateTime getEndOfDay(LocalDate dateTime) {
        return dateTime == null ? MAX_EPOCH_TIME : dateTime.atTime(LocalTime.MAX);
    }
}
