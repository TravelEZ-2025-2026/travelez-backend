package com.example.travelez.backend.moderation.service;

import com.example.travelez.backend.moderation.dto.internal.ContentCheckResult;
import com.example.travelez.backend.moderation.model.enums.ModerationTargetType;

public interface ContentModerationService {
    
    ContentCheckResult checkKeywords(String title, String content);
    
    void submitForAIModeration(Long targetId, ModerationTargetType targetType);
}
