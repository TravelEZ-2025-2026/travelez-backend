package com.example.travelez.backend.report.listener;

import com.example.travelez.backend.posts.event.PostsStatusChangedEvent;
import com.example.travelez.backend.posts.model.enums.PostStatusAction;
import com.example.travelez.backend.report.event.ReportProcessedEvent;
import com.example.travelez.backend.report.model.enums.ReportStatus;
import com.example.travelez.backend.report.service.ReportService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class PostsReportListener {

    private final ReportService reportService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPostStatusChanged(PostsStatusChangedEvent event) {
        if (event.getAction() == PostStatusAction.BANNED) {
            reportService.resolveReportsForPost(event.getPosts().getId(), ReportStatus.RESOLVED);
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onReportProcessed(ReportProcessedEvent event) {
        // for (Report report : event.getProcessedReports()) {
        //     if (report.getStatus() == ReportStatus.REJECTED) {
        //         reportService.resolveReportsForPost(report.getTargetId(), ReportStatus.REJECTED);
        //     }
        // }
    }
}
