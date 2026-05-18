package com.example.travelez.backend.report.service.impl;

import com.example.travelez.backend.common.api.CommonPage;
import com.example.travelez.backend.common.api.ResultCode;
import com.example.travelez.backend.common.exception.ApiException;
import com.example.travelez.backend.posts.repository.PostsRepository;
import com.example.travelez.backend.report.dto.request.AdminRejectReportRequest;
import com.example.travelez.backend.report.dto.request.ReportedItemsFilterRequest;
import com.example.travelez.backend.report.dto.response.PostReportDetailResponse;
import com.example.travelez.backend.report.dto.response.PostReportsResponse;
import com.example.travelez.backend.report.dto.response.ReportedPostResponse;
import com.example.travelez.backend.report.event.ReportProcessedEvent;
import com.example.travelez.backend.report.handler.ReportHandler;
import com.example.travelez.backend.report.mapper.ReportMapper;
import com.example.travelez.backend.report.model.Report;
import com.example.travelez.backend.report.model.enums.ReportStatus;
import com.example.travelez.backend.report.model.enums.ReportTargetType;
import com.example.travelez.backend.report.repository.ReportRepository;
import com.example.travelez.backend.report.repository.projection.ReportedPostProjection;
import com.example.travelez.backend.report.service.AdminReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminReportServiceImpl implements AdminReportService {

    private final ApplicationEventPublisher eventPublisher;

    private final Map<ReportTargetType, ReportHandler> reportHandlers;

    private final ReportRepository reportRepository;
    
    private final PostsRepository postsRepository;
    
    private final ReportMapper reportMapper;
    
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

    @Override
    @Transactional(readOnly = true)
    public CommonPage<ReportedPostResponse> getReportedItems(ReportedItemsFilterRequest filter, Pageable pageable) {
        Page<ReportedPostProjection> projections;
        
        // If showAll = false (default), only get posts with PENDING reports
        // If showAll = true, get all reported posts
        if (Boolean.TRUE.equals(filter.getShowAll())) {
            projections = reportRepository.findAllReportedPosts(pageable);
        } else {
            projections = reportRepository.findPostsWithPendingReports(pageable);
        }
        
        List<ReportedPostResponse> responses = projections.getContent().stream()
            .map(p -> ReportedPostResponse.builder()
                .postId(p.getPostId())
                .title(p.getTitle())
                .content(p.getContent())
                .authorId(p.getAuthorId())
                .authorName(p.getAuthorName())
                .reportCount(p.getReportCount())
                .pendingReportCount(p.getPendingReportCount())
                .firstReportedAt(p.getFirstReportedAt())
                .latestReportedAt(p.getLatestReportedAt())
                .postStatus(p.getPostStatus())
                .build())
            .toList();
        
        return new CommonPage<>(responses, projections.getTotalPages(), projections.getTotalElements(), 
                pageable.getPageSize(), projections.getNumber(), projections.isEmpty());
    }

    @Override
    @Transactional(readOnly = true)
    public PostReportsResponse getPostReports(Long postId, ReportStatus status, Pageable pageable) {
        // Verify post exists
        if (!postsRepository.existsById(postId)) {
            throw new ApiException(ResultCode.NOT_FOUND, "Post not found");
        }

        // Get reports with optional status filter
        Page<Report> reportPage = status != null 
            ? reportRepository.findByPostIdAndStatus(postId, status, pageable)
            : reportRepository.findByPostId(postId, pageable);

        // Map to response DTOs
        List<PostReportDetailResponse> reportDetails = reportPage.getContent().stream()
            .map(reportMapper::toPostReportDetailResponse)
            .toList();

        CommonPage<PostReportDetailResponse> reportsPage = new CommonPage<>(
            reportDetails, 
            reportPage.getTotalPages(), 
            reportPage.getTotalElements(),
            pageable.getPageSize(), 
            reportPage.getNumber(), 
            reportPage.isEmpty()
        );

        // Get counts
        Long totalReports = reportRepository.countByPostId(postId);
        Long pendingReports = reportRepository.countByPostIdAndStatus(postId, ReportStatus.PENDING);

        return PostReportsResponse.builder()
            .totalReports(totalReports)
            .pendingReports(pendingReports)
            .reports(reportsPage)
            .build();
    }
}
