package com.example.travelez.backend.itinerary.dto;

import lombok.Data;

import java.util.List;

@Data
public class ParsedIntent {
    private String destination;
    private int durationDays;
    private List<String> tags;
}
