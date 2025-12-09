package com.example.travelez.backend.itinerary.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

@Data
public class ItineraryResponse {
    private String tripTitle;
    private List<String> destinationCities;
    private String reasoningSummary;
    private List<DayPlan> days;

    @Data
    public static class DayPlan {
        private int dayIndex;
        private String date;
        private String theme;
        private List<Activity> activities;
    }

    @Data
    public static class Activity {
        private long id;
        private String title;
        private String startTime;
        private String endTime;
        private String price;

        private String activityName;
        private String activityType;
        private String address;
        private String aiTip;
        private String image;
        private Double lat;
        private Double lng;
    }
}
