package com.example.travelez.backend.report.dto.response;

import com.example.travelez.backend.posts.model.enums.PostStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportedPostResponse {
    private Long postId;
    private String title;
    private String content;
    private Long authorId;
    private String authorName;
    private Long reportCount; // Total reports (all statuses)
    private Long pendingReportCount; // Only PENDING reports
    private LocalDateTime firstReportedAt;
    private LocalDateTime latestReportedAt;
    private PostStatus postStatus;
}
