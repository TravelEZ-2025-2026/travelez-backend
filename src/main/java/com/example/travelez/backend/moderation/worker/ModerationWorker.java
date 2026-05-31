package com.example.travelez.backend.moderation.worker;

import com.example.travelez.backend.media.model.Media;
import com.example.travelez.backend.moderation.dto.internal.AIModerationResponse;
import com.example.travelez.backend.moderation.dto.internal.ModerationMessage;
import com.example.travelez.backend.moderation.model.enums.ModerationTargetType;
import com.example.travelez.backend.moderation.service.AIModerationService;
import com.example.travelez.backend.moderation.service.ModerationAlertService;
import com.example.travelez.backend.posts.model.Posts;
import com.example.travelez.backend.posts.repository.PostsRepository;
import com.example.travelez.backend.review.model.Review;
import com.example.travelez.backend.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class ModerationWorker {

    private final AIModerationService aiModerationService;
    private final ModerationAlertService alertService;
    private final PostsRepository postsRepository;
    private final ReviewRepository reviewRepository;

    @RabbitListener(queues = "${moderation.queue.name}")
    public void processModerationTask(ModerationMessage message) {
        log.info("Processing moderation task for {} {}", message.getTargetType(), message.getTargetId());
        
        try {
            String text = extractText(message.getTargetId(), message.getTargetType());
            // List<String> imageUrls = extractImageUrls(message.getTargetId(), message.getTargetType());
            
            if (text == null || text.isBlank()) {
                log.warn("No text content found for {} {}", message.getTargetType(), message.getTargetId());
                return;
            }
            
            AIModerationResponse aiResult = aiModerationService.analyzeContent(text, null);
            
            if (!aiResult.isSafe()) {
                log.warn("AI detected violation for {} {}: {}", 
                        message.getTargetType(), 
                        message.getTargetId(), 
                        aiResult.getViolationType());
                
                alertService.createAlert(message.getTargetId(), message.getTargetType(), aiResult);
            } else {
                log.info("Content is safe for {} {}", message.getTargetType(), message.getTargetId());
            }
            
        } catch (Exception e) {
            log.error("Failed to process moderation task for {} {}: {}", 
                    message.getTargetType(), 
                    message.getTargetId(), 
                    e.getMessage(), e);
            throw e;
        }
    }

    private String extractText(Long targetId, ModerationTargetType targetType) {
        return switch (targetType) {
            case POST -> {
                Posts post = postsRepository.findById(targetId).orElse(null);
                if (post == null) yield null;
                String title = post.getTitle() != null ? post.getTitle() : "";
                String content = post.getContent() != null ? post.getContent() : "";
                yield title + " " + content;
            }
            case REVIEW -> {
                Review review = reviewRepository.findById(targetId).orElse(null);
                yield review != null ? review.getContent() : null;
            }
            case POI -> null;
        };
    }

    private List<String> extractImageUrls(Long targetId, ModerationTargetType targetType) {
        return switch (targetType) {
            case POST -> {
                Posts post = postsRepository.findByIdWithMedias(targetId).orElse(null);
                if (post == null || post.getMedias() == null) {
                    yield List.of();
                }
                yield post.getMedias().stream()
                        .filter(media -> media.getType() != null && 
                                media.getType().toString().startsWith("IMAGE"))
                        .map(Media::getUrl)
                        .collect(Collectors.toList());
            }
            case REVIEW -> {
                Review review = reviewRepository.findByIdWithMedias(targetId).orElse(null);
                if (review == null || review.getMedias() == null) {
                    yield List.of();
                }
                yield review.getMedias().stream()
                        .filter(media -> media.getType() != null && 
                                media.getType().toString().startsWith("IMAGE"))
                        .map(Media::getUrl)
                        .collect(Collectors.toList());
            }
            case POI -> List.of();
        };
    }
}
