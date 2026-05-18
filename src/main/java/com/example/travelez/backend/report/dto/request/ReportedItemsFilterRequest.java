package com.example.travelez.backend.report.dto.request;

import lombok.Data;

@Data
public class ReportedItemsFilterRequest {
    private Boolean showAll = false; // false = only posts with PENDING reports (default), true = all reported posts
    private String sortBy = "reportCount"; // reportCount or createdAt
    private String sortDirection = "DESC"; // ASC or DESC
}
