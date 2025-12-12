package com.example.travelez.backend.itinerary.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class DayPlan {
    private int dayIndex;
    private String date;
    private String theme;
    private List<ActivityDTO> activities;
}
