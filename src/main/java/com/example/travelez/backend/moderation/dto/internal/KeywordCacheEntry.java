package com.example.travelez.backend.moderation.dto.internal;

import com.example.travelez.backend.moderation.model.enums.KeywordSeverity;
import com.example.travelez.backend.moderation.model.enums.ViolationType;

public record KeywordCacheEntry(
        Long id,
        String keyword,
        ViolationType violationType,
        KeywordSeverity severity,
        String description
) {}
