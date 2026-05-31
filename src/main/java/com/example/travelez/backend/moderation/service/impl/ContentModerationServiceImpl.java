package com.example.travelez.backend.moderation.service.impl;

import com.example.travelez.backend.moderation.dto.internal.ContentCheckResult;
import com.example.travelez.backend.moderation.dto.internal.ModerationMessage;
import com.example.travelez.backend.moderation.filter.KeywordFilter;
import com.example.travelez.backend.moderation.model.enums.ModerationTargetType;
import com.example.travelez.backend.moderation.service.ContentModerationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContentModerationServiceImpl implements ContentModerationService {

    private final KeywordFilter keywordFilter;
    private final RabbitTemplate rabbitTemplate;
    
    @Value("${moderation.queue.name}")
    private String queueName;

    @Override
    public ContentCheckResult checkKeywords(String title, String content) {
        String combinedText = (title != null ? title + " " : "") + (content != null ? content : "");
        ContentCheckResult result = keywordFilter.checkContent(combinedText);
        
        if (!result.isSafe()) {
            log.warn("Content blocked by keyword filter: {}", result.getMatchedKeyword());
        }
        
        return result;
    }

    @Override
    public void submitForAIModeration(Long targetId, ModerationTargetType targetType) {
        ModerationMessage message = ModerationMessage.builder()
                .targetId(targetId)
                .targetType(targetType)
                .build();
        
        rabbitTemplate.convertAndSend(queueName, message);
        log.info("Submitted {} {} for AI moderation", targetType, targetId);
    }
}
