package com.example.travelez.backend.moderation.service;

import com.example.travelez.backend.moderation.dto.internal.AIModerationResponse;

import java.util.List;

public interface AIModerationService {
    
    AIModerationResponse analyzeContent(String text, List<String> imageUrls);
}
