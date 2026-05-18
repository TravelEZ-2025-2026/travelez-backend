package com.example.travelez.backend.report.dto.response;

import com.example.travelez.backend.report.model.enums.ReportReason;
import com.example.travelez.backend.report.model.enums.ReportStatus;
import com.example.travelez.backend.users.dto.response.UserSummaryResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostReportDetailResponse {
    private Long id;
    private UserSummaryResponse reporter;
    private ReportReason reason;
    private String reasonDetail;
    private ReportStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
