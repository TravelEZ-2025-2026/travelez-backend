package com.example.travelez.backend.itinerary.util;

import org.mapstruct.Named;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalTime;

@Component
public class ItineraryUtils {

    @Named("parseTime")
    public static LocalTime parseTime(String timeString) {
        try {
            if (timeString == null) return null;
            String cleanTime = timeString.split("-")[0].trim();
            return LocalTime.parse(cleanTime);
        } catch (Exception e) { return null; }
    }

    @Named("formatTime")
    public static String formatTime(LocalTime time) {
        return time == null ? null : time.toString();
    }

    @Named("parsePrice")
    public static BigDecimal parsePrice(String priceStr) {
        try {
            if (priceStr != null && !priceStr.equalsIgnoreCase("Free")) {
                String cleanPrice = priceStr.replaceAll("[^0-9.]", "");
                if (cleanPrice.isEmpty()) return BigDecimal.ZERO;
                return new BigDecimal(cleanPrice);
            }
        } catch (Exception e) { /* ignore */ }
        return BigDecimal.ZERO;
    }

    @Named("calculateTimeOfDay")
    public static String calculateTimeOfDay(LocalTime time) {
        if (time == null) return "ANYTIME";
        int hour = time.getHour();
        if (hour >= 5 && hour < 12) return "MORNING";
        if (hour >= 12 && hour < 18) return "AFTERNOON";
        return "EVENING";
    }

    @Named("calcTimeOfDayFromString")
    public String calcTimeOfDayFromString(String timeString) {
        return calculateTimeOfDay(parseTime(timeString));
    }
}
