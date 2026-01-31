package com.example.travelez.backend.itinerary.dto.response.utils;

import lombok.Data;

import java.util.List;

@Data
public class DayPlan {
    private int dayIndex;
    private String date;
    private List<ActivityDTO> activities;
}
