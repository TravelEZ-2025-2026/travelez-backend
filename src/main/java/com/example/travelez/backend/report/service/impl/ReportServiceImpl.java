package com.example.travelez.backend.report.service.impl;

import com.example.travelez.backend.common.api.ResultCode;
import com.example.travelez.backend.common.exception.ApiException;
import com.example.travelez.backend.common.exception.Asserts;
import com.example.travelez.backend.common.utils.SecurityUtils;
import com.example.travelez.backend.infrastructure.filestorage.dto.UploadFileResult;
import com.example.travelez.backend.media.dto.enums.MediaTarget;
import com.example.travelez.backend.media.service.MediaService;
import com.example.travelez.backend.report.dto.request.ReportCreateRequest;
import com.example.travelez.backend.report.handler.ReportHandler;
import com.example.travelez.backend.report.mapper.ReportMapper;
import com.example.travelez.backend.report.model.Report;
import com.example.travelez.backend.report.model.enums.ReportStatus;
import com.example.travelez.backend.report.model.enums.ReportTargetType;
import com.example.travelez.backend.report.repository.ReportRepository;
import com.example.travelez.backend.report.service.ReportService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportServiceImpl implements ReportService {
    
    private final TransactionTemplate transactionTemplate;

    private final Map<ReportTargetType, ReportHandler> reportHandlers;
    
    private final MediaService mediaService;

    private final ReportRepository reportRepository;

    private final ReportMapper reportMapper;

    @Override
    public void createReport(ReportCreateRequest request) {
        List<UploadFileResult> uploadedFiles = uploadFile(request.getFiles());
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            ReportHandler handler = reportHandlers.get(request.getTargetType());
            transactionTemplate.execute(status -> {
                Report report = handler.buildReport(request, uploadedFiles, userId);
                Report savedReport = reportRepository.save(report);
                mediaService.attachMediasToEntity(uploadedFiles, MediaTarget.REPORT, savedReport.getId());
                return savedReport;
            });
        } catch (Exception e) {
            List<String> fileNames = uploadedFiles.stream().map(UploadFileResult::getCloudName).toList();
            mediaService.cleanupFilesAsync(fileNames);
            if (e instanceof ApiException) {
                throw (ApiException) e;
            }
            Asserts.fail(ResultCode.INTERNAL_SERVER_ERROR, "Failed to create report: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void resolveReportsForPost(Long postId, ReportStatus newStatus) {
        // Cập nhật tất cả reports PENDING của post này thành RESOLVED hoặc REJECTED
        reportRepository.updateStatusByPostId(postId, newStatus, ReportStatus.PENDING);
    }

    private List<UploadFileResult> uploadFile(List<MultipartFile> files) {
        return mediaService.uploadFilesParallel(files, "report/" + UUID.randomUUID().toString() + "/");
    }

}
