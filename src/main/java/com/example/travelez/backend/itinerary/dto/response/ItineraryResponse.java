package com.example.travelez.backend.itinerary.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class ItineraryResponse {
    private String tripTitle;
    private String reasoningSummary; // Lý do ngắn gọn AI chọn lịch trình này
    private List<DayPlan> days;

    @Data
    public static class DayPlan {
        private int dayIndex;
        private String date; // Format YYYY-MM-DD
        private String theme; // Chủ đề chính trong ngày
        private List<Activity> activities;
    }

    @Data
    public static class Activity {
        private String timeSlot;   // VD: "08:00 - 10:00"
        private long locationId;   // ID của POI trong DB (Quan trọng để join bảng)
        private String locationName;
        private String activityName;
        private String activityType; // VD: Sightseeing, Dining
        private String notes;      // Ghi chú của AI (VD: "Nên đặt bàn trước")
    }
}
