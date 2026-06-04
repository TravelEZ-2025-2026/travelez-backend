package com.example.travelez.backend.chat.scheduler;


import com.example.travelez.backend.chat.repository.MessageRepository;
import com.example.travelez.backend.media.model.Media;
import com.example.travelez.backend.media.repository.MediaRepository;
import com.example.travelez.backend.media.service.MediaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class MessageCleanupTask {

    @Value("${app.task.cleanup.batch-size}")
    private int batchSize;

    @Value("${app.task.cleanup.days-to-keep}")
    private int daysToKeep;

    private final TransactionTemplate transactionTemplate;

    private final MediaService mediaService;

    private final MessageRepository messageRepository;

    private final MediaRepository mediaRepository;

    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupRecalledMessagesMedia() {
        log.info("CRON JOB: Bắt đầu dọn dẹp file của tin nhắn đã thu hồi...");
        LocalDateTime thresholdDate = LocalDateTime.now().minusDays(daysToKeep);
        int totalDeleted = 0;
        while (true) {
            Pageable limit = PageRequest.of(0, batchSize);
            List<Long> batchMessageIds = messageRepository.findRecalledMessageIdsBefore(thresholdDate, limit);
            if (batchMessageIds.isEmpty()) break;
            List<Object[]> rawMediasToDelete = mediaRepository.findAllByMessageIds(batchMessageIds);
            List<Media> mediasToDelete = rawMediasToDelete.stream().map(row -> (Media) row[1]).toList();
            List<String> cloudNamesToDelete = mediasToDelete.stream().map(Media::getCloudName).toList();
            try {
                transactionTemplate.execute(status -> {
                    if (!mediasToDelete.isEmpty()) {
                        mediaRepository.deleteAllInBatch(mediasToDelete);
                    }
                    messageRepository.deleteAllByIdInBatch(batchMessageIds);
                    return null;
                });

            } catch (Exception e) {
                log.error("CRON JOB: Lỗi khi xóa DB ở batch này, dừng job.", e.getMessage());
                break;
            }
            if (!cloudNamesToDelete.isEmpty()) {
                mediaService.cleanupFilesAsync(cloudNamesToDelete);
            }
            totalDeleted += batchMessageIds.size();
            log.info("CRON JOB: Đã xóa {} tin nhắn ở batch này.", batchMessageIds.size());
        }
        log.info("CRON JOB HOÀN TẤT: Đã xóa tổng cộng {} tin nhắn.", totalDeleted);
    }
}

