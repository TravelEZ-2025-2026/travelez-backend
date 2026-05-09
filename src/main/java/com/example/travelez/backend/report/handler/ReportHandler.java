package com.example.travelez.backend.report.handler;

import java.util.List;

import com.example.travelez.backend.infrastructure.filestorage.dto.UploadFileResult;
import com.example.travelez.backend.report.dto.request.ReportCreateRequest;
import com.example.travelez.backend.report.model.Report;
import com.example.travelez.backend.report.model.enums.ReportTargetType;

public interface ReportHandler {
    ReportTargetType getSupportedType();
    Report buildReport(ReportCreateRequest request, List<UploadFileResult> uploadedFiles, Long userId);
    List<Report> getPendingReports(Long targetId);
}
