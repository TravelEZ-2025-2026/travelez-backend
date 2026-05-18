package com.example.travelez.backend.report.service;

import com.example.travelez.backend.report.model.enums.ReportStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;

import com.example.travelez.backend.common.api.CommonPage;
import com.example.travelez.backend.report.dto.request.AdminRejectReportRequest;
import com.example.travelez.backend.report.dto.request.ReportedItemsFilterRequest;
import com.example.travelez.backend.report.dto.response.PostReportsResponse;
import com.example.travelez.backend.report.dto.response.ReportedPostResponse;

@PreAuthorize("hasRole('ADMIN')")
public interface AdminReportService {
    void rejectReports(AdminRejectReportRequest request);
    
    CommonPage<ReportedPostResponse> getReportedItems(ReportedItemsFilterRequest filter, Pageable pageable);
    
    PostReportsResponse getPostReports(Long postId, ReportStatus status, Pageable pageable);
}
