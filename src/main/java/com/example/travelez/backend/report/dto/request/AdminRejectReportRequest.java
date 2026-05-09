package com.example.travelez.backend.report.dto.request;

import com.example.travelez.backend.report.model.enums.ReportTargetType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AdminRejectReportRequest {
    @NotNull(message = "Target type is required")
    private ReportTargetType targetType; // POST, REVIEW...

    @NotNull(message = "Target ID is required")
    private Long targetId;
}
