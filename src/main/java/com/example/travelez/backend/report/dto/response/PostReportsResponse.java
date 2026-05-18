package com.example.travelez.backend.report.dto.response;

import com.example.travelez.backend.common.api.CommonPage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostReportsResponse {
    private Long totalReports;
    private Long pendingReports;
    private CommonPage<PostReportDetailResponse> reports;
}
