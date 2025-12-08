package com.example.travelez.backend.itinerary.dto.response;

import lombok.Data;

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
        private String timeSlot;
        private long locationId;
        private String locationName;
        private String activityName;
        private String activityType;
        private String notes;
        private String locationImage;
    }
}
