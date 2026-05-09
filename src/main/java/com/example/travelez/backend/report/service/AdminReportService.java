package com.example.travelez.backend.report.service;

import org.springframework.security.access.prepost.PreAuthorize;

import com.example.travelez.backend.report.dto.request.AdminRejectReportRequest;

@PreAuthorize("hasRole('ADMIN')")
public interface AdminReportService {
    void rejectReports(AdminRejectReportRequest request);
}
