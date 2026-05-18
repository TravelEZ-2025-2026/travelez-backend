package com.example.travelez.backend.dashboard.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class DashboardActivityResponse {
    private LocalDateTime time;
    private String category;
    private String description;
    private String status;
}
