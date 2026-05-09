package com.example.travelez.backend.report.handler;

import com.example.travelez.backend.common.api.ResultCode;
import com.example.travelez.backend.common.exception.ApiException;
import com.example.travelez.backend.infrastructure.filestorage.dto.UploadFileResult;
import com.example.travelez.backend.posts.model.Posts;
import com.example.travelez.backend.posts.model.enums.PostStatus;
import com.example.travelez.backend.posts.repository.PostsRepository;
import com.example.travelez.backend.report.dto.request.ReportCreateRequest;
import com.example.travelez.backend.report.model.Report;
import com.example.travelez.backend.report.model.enums.ReportStatus;
import com.example.travelez.backend.report.model.enums.ReportTargetType;
import com.example.travelez.backend.report.repository.ReportRepository;
import com.example.travelez.backend.users.model.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PostsReportHandler implements ReportHandler {

    private final PostsRepository postsRepository;
    private final ReportRepository reportRepository;

    @Override
    public ReportTargetType getSupportedType() {
        return ReportTargetType.POST;
    }

    @Override
    public Report buildReport(ReportCreateRequest request, List<UploadFileResult> uploadedFiles, Long userId) {
        boolean postExists = postsRepository.existsByIdAndStatus(request.getTargetId(), PostStatus.PUBLISHED);
        if (!postExists) {
            throw new ApiException(ResultCode.NOT_FOUND, "Post not found");
        }
        return Report.builder()
            .reporter(User.builder().id(userId).build())
            .targetType(ReportTargetType.POST)
            .reason(request.getReason())
            .reasonDetail(request.getReasonDetail())
            .status(ReportStatus.PENDING)
            .post(Posts.builder().id(request.getTargetId()).build())
            .build();
    }

    @Override
    public List<Report> getPendingReports(Long targetId) {
        return reportRepository.findByPostIdAndStatus(targetId, ReportStatus.PENDING);
    }
}
