package com.example.travelez.backend.report.service.impl;

import com.example.travelez.backend.report.dto.request.AdminRejectReportRequest;
import com.example.travelez.backend.report.event.ReportProcessedEvent;
import com.example.travelez.backend.report.handler.ReportHandler;
import com.example.travelez.backend.report.model.Report;
import com.example.travelez.backend.report.model.enums.ReportStatus;
import com.example.travelez.backend.report.model.enums.ReportTargetType;
import com.example.travelez.backend.report.repository.ReportRepository;
import com.example.travelez.backend.report.service.AdminReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminReportServiceImpl implements AdminReportService {

    private final ApplicationEventPublisher eventPublisher;

    private final Map<ReportTargetType, ReportHandler> reportHandlers;

    private final ReportRepository reportRepository;
    
    @Override
    @Transactional
    public void rejectReports(AdminRejectReportRequest request) {
        ReportHandler handler = reportHandlers.get(request.getTargetType());
        List<Report> pendingReports = handler.getPendingReports(request.getTargetId());
        if (pendingReports.isEmpty()) return;

        for (Report report : pendingReports) {
            report.setStatus(ReportStatus.REJECTED);
        }
        reportRepository.saveAll(pendingReports);

        eventPublisher.publishEvent(new ReportProcessedEvent(pendingReports, ReportStatus.REJECTED));
    }
}
