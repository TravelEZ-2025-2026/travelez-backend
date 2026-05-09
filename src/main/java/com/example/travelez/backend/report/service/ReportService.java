package com.example.travelez.backend.report.service;

import com.example.travelez.backend.report.dto.request.ReportCreateRequest;
import com.example.travelez.backend.report.model.enums.ReportStatus;

public interface ReportService {
    void createReport(ReportCreateRequest request);

    void resolveReportsForPost(Long postId, ReportStatus newStatus);
}
