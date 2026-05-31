package com.example.travelez.backend.moderation.service.impl;

import com.example.travelez.backend.common.api.CommonPage;
import com.example.travelez.backend.common.api.ResultCode;
import com.example.travelez.backend.common.exception.ApiException;
import com.example.travelez.backend.common.utils.SecurityUtils;
import com.example.travelez.backend.moderation.dto.internal.AIModerationResponse;
import com.example.travelez.backend.moderation.dto.request.AlertReviewRequest;
import com.example.travelez.backend.moderation.dto.request.ModerationAlertSearchRequest;
import com.example.travelez.backend.moderation.dto.response.ModerationAlertResponse;
import com.example.travelez.backend.moderation.dto.response.ModerationDashboardResponse;
import com.example.travelez.backend.moderation.mapper.ModerationAlertMapper;
import com.example.travelez.backend.moderation.model.ModerationAlert;
import com.example.travelez.backend.moderation.model.enums.AlertStatus;
import com.example.travelez.backend.moderation.model.enums.ModerationTargetType;
import com.example.travelez.backend.moderation.model.enums.ViolationType;
import com.example.travelez.backend.moderation.repository.BannedKeywordRepository;
import com.example.travelez.backend.moderation.repository.ModerationAlertRepository;
import com.example.travelez.backend.moderation.service.ModerationAlertService;
import com.example.travelez.backend.posts.event.PostsStatusChangedEvent;
import com.example.travelez.backend.posts.model.Posts;
import com.example.travelez.backend.posts.model.enums.AiScanStatus;
import com.example.travelez.backend.posts.model.enums.PostStatus;
import com.example.travelez.backend.posts.repository.PostsRepository;
import com.example.travelez.backend.review.model.Review;
import com.example.travelez.backend.review.model.enums.ReviewStatus;
import com.example.travelez.backend.review.repository.ReviewRepository;
import com.example.travelez.backend.users.model.User;
import com.example.travelez.backend.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ModerationAlertServiceImpl implements ModerationAlertService {

    private final ModerationAlertRepository alertRepository;
    private final BannedKeywordRepository keywordRepository;
    private final PostsRepository postsRepository;
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final ModerationAlertMapper mapper;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public void createAlert(Long targetId, ModerationTargetType targetType, AIModerationResponse aiResult) {
        ModerationAlert alert = ModerationAlert.builder()
                .targetId(targetId)
                .targetType(targetType)
                .violationType(aiResult.getViolationTypeEnum())
                .confidenceScore(aiResult.getConfidenceScore())
                .reason(aiResult.getReason())
                .status(AlertStatus.PENDING)
                .build();
        
        alertRepository.save(alert);
        
        // Update target status to FLAGGED
        updateTargetStatus(targetId, targetType, true);
        
        log.info("Created moderation alert for {} {}", targetType, targetId);
    }

    @Override
    public ModerationAlertResponse getAlertById(Long id) {
        ModerationAlert alert = alertRepository.findById(id)
                .orElseThrow(() -> new ApiException(ResultCode.NOT_FOUND, "Alert not found"));
        
        ModerationAlertResponse response = mapper.toResponse(alert);
        enrichAlertResponse(response, alert);
        return response;
    }

    @Override
    public CommonPage<ModerationAlertResponse> searchAlerts(ModerationAlertSearchRequest request, Pageable pageable) {
        Specification<ModerationAlert> spec = buildSpecification(request);
        Page<ModerationAlert> page = alertRepository.findAll(spec, pageable);
        
        List<ModerationAlertResponse> responses = page.getContent().stream()
                .map(alert -> {
                    ModerationAlertResponse response = mapper.toResponse(alert);
                    enrichAlertResponse(response, alert);
                    return response;
                })
                .toList();
        
        return new CommonPage<>(responses, page.getTotalPages(), page.getTotalElements(),
                page.getSize(), page.getNumber(), page.isEmpty());
    }

    @Override
    @Transactional
    public void approveAlert(Long id, AlertReviewRequest request) {
        ModerationAlert alert = alertRepository.findById(id)
                .orElseThrow(() -> new ApiException(ResultCode.NOT_FOUND, "Alert not found"));
        
        if (alert.getStatus() != AlertStatus.PENDING) {
            throw new ApiException(ResultCode.BAD_REQUEST, "Alert already reviewed");
        }
        
        Long currentUserId = SecurityUtils.getCurrentUserId();
        User reviewer = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ApiException(ResultCode.NOT_FOUND, "User not found"));
        
        alert.setStatus(AlertStatus.APPROVED);
        alert.setReviewedBy(reviewer);
        alert.setReviewedAt(LocalDateTime.now());
        alert.setAdminNote(request.getAdminNote());
        alertRepository.save(alert);
        
        // Update target status back to PUBLISHED
        updateTargetStatus(alert.getTargetId(), alert.getTargetType(), false);
        
        log.info("Alert {} approved by user {}", id, currentUserId);
    }

    @Override
    @Transactional
    public void banAlert(Long id, AlertReviewRequest request) {
        ModerationAlert alert = alertRepository.findById(id)
                .orElseThrow(() -> new ApiException(ResultCode.NOT_FOUND, "Alert not found"));
        
        if (alert.getStatus() != AlertStatus.PENDING) {
            throw new ApiException(ResultCode.BAD_REQUEST, "Alert already reviewed");
        }
        
        Long currentUserId = SecurityUtils.getCurrentUserId();
        User reviewer = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ApiException(ResultCode.NOT_FOUND, "User not found"));
        
        alert.setStatus(AlertStatus.BANNED);
        alert.setReviewedBy(reviewer);
        alert.setReviewedAt(LocalDateTime.now());
        alert.setAdminNote(request.getAdminNote());
        alertRepository.save(alert);
        
        // Update target status to BANNED and publish event
        banTarget(alert.getTargetId(), alert.getTargetType());
        
        log.info("Alert {} banned by user {}", id, currentUserId);
    }

    @Override
    public ModerationDashboardResponse getDashboard() {
        long totalAlerts = alertRepository.count();
        long pendingAlerts = alertRepository.countByStatus(AlertStatus.PENDING);
        long approvedAlerts = alertRepository.countByStatus(AlertStatus.APPROVED);
        long bannedAlerts = alertRepository.countByStatus(AlertStatus.BANNED);
        long totalKeywords = keywordRepository.count();
        long activeKeywords = keywordRepository.countByIsActiveTrue();
        
        Map<ViolationType, Long> violationStats = new HashMap<>();
        List<Object[]> violationData = alertRepository.countByViolationTypeGrouped();
        for (Object[] row : violationData) {
            violationStats.put((ViolationType) row[0], (Long) row[1]);
        }
        
        Map<ModerationTargetType, Long> targetStats = new HashMap<>();
        List<Object[]> targetData = alertRepository.countByTargetTypeGrouped();
        for (Object[] row : targetData) {
            targetStats.put((ModerationTargetType) row[0], (Long) row[1]);
        }
        
        return ModerationDashboardResponse.builder()
                .totalAlerts(totalAlerts)
                .pendingAlerts(pendingAlerts)
                .approvedAlerts(approvedAlerts)
                .bannedAlerts(bannedAlerts)
                .totalBannedKeywords(totalKeywords)
                .activeKeywords(activeKeywords)
                .violationTypeStats(violationStats)
                .targetTypeStats(targetStats)
                .build();
    }

    private void updateTargetStatus(Long targetId, ModerationTargetType targetType, boolean flag) {
        switch (targetType) {
            case POST -> {
                postsRepository.findById(targetId).ifPresent(post -> {
                    post.setAiScanStatus(flag ? AiScanStatus.FLAGGED : AiScanStatus.CLEAN);
                    postsRepository.save(post);
                    log.info("Updated post {} aiScanStatus to {}", targetId, flag ? "FLAGGED" : "CLEAN");
                });
            }
            case REVIEW -> {
                reviewRepository.findById(targetId).ifPresent(review -> {
                    review.setAiScanStatus(flag ? AiScanStatus.FLAGGED : AiScanStatus.CLEAN);
                    reviewRepository.save(review);
                    log.info("Updated review {} aiScanStatus to {}", targetId, flag ? "FLAGGED" : "CLEAN");
                });
            }
            case POI -> log.info("POI moderation status update not implemented yet");
        }
    }

    private void banTarget(Long targetId, ModerationTargetType targetType) {
        switch (targetType) {
            case POST -> {
                Posts post = postsRepository.findById(targetId).orElse(null);
                if (post != null) {
                    PostStatus oldStatus = post.getStatus();
                    post.setStatus(PostStatus.BANNED);
                    post.setAiScanStatus(AiScanStatus.FLAGGED);
                    postsRepository.save(post);

                    eventPublisher.publishEvent(new PostsStatusChangedEvent(
                            post,
                            SecurityUtils.getCurrentUserId(),
                            post.getUser().getId(),
                            oldStatus,
                            PostStatus.BANNED,
                            com.example.travelez.backend.posts.model.enums.PostStatusAction.BANNED,
                            "Banned by AI moderation"
                    ));
                }
            }
            case REVIEW -> {
                Review review = reviewRepository.findById(targetId).orElse(null);
                if (review != null) {
                    review.setStatus(ReviewStatus.BANNED);
                    review.setAiScanStatus(AiScanStatus.FLAGGED);
                    reviewRepository.save(review);
                }
            }
        }
    }

    private void enrichAlertResponse(ModerationAlertResponse response, ModerationAlert alert) {
        switch (alert.getTargetType()) {
            case POST -> {
                postsRepository.findById(alert.getTargetId()).ifPresent(post -> {
                    response.setTargetTitle(post.getTitle());
                    response.setTargetContent(post.getContent());
                    response.setTargetAuthorName(post.getUser().getFullName());
                    response.setTargetAuthorId(post.getUser().getId());
                });
            }
            case REVIEW -> {
                reviewRepository.findById(alert.getTargetId()).ifPresent(review -> {
                    response.setTargetContent(review.getContent());
                    if (review.getTraveler() != null) {
                        response.setTargetAuthorName(review.getTraveler().getFullName());
                        response.setTargetAuthorId(review.getTraveler().getId());
                    }
                });
            }
        }
    }

    private Specification<ModerationAlert> buildSpecification(ModerationAlertSearchRequest request) {
        return (root, query, cb) -> {
            var predicates = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();
            
            if (request.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), request.getStatus()));
            }
            
            if (request.getViolationType() != null) {
                predicates.add(cb.equal(root.get("violationType"), request.getViolationType()));
            }
            
            if (request.getTargetType() != null) {
                predicates.add(cb.equal(root.get("targetType"), request.getTargetType()));
            }
            
            if (request.getFromDate() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), request.getFromDate()));
            }
            
            if (request.getToDate() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), request.getToDate()));
            }
            
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }
}
