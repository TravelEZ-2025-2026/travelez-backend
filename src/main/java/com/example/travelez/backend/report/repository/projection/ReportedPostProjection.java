package com.example.travelez.backend.report.repository.projection;

import com.example.travelez.backend.posts.model.enums.PostStatus;

import java.time.LocalDateTime;

public interface ReportedPostProjection {
    Long getPostId();
    String getTitle();
    String getContent();
    Long getAuthorId();
    String getAuthorName();
    Long getReportCount(); // Total reports
    Long getPendingReportCount(); // Only PENDING reports
    LocalDateTime getFirstReportedAt();
    LocalDateTime getLatestReportedAt();
    PostStatus getPostStatus();
}
