package com.example.travelez.backend.moderation.service;

import com.example.travelez.backend.common.api.CommonPage;
import com.example.travelez.backend.moderation.dto.internal.AIModerationResponse;
import com.example.travelez.backend.moderation.dto.request.AlertReviewRequest;
import com.example.travelez.backend.moderation.dto.request.ModerationAlertSearchRequest;
import com.example.travelez.backend.moderation.dto.response.ModerationAlertResponse;
import com.example.travelez.backend.moderation.dto.response.ModerationDashboardResponse;
import com.example.travelez.backend.moderation.model.enums.ModerationTargetType;
import org.springframework.data.domain.Pageable;

public interface ModerationAlertService {
    
    void createAlert(Long targetId, ModerationTargetType targetType, AIModerationResponse aiResult);
    
    ModerationAlertResponse getAlertById(Long id);
    
    CommonPage<ModerationAlertResponse> searchAlerts(ModerationAlertSearchRequest request, Pageable pageable);
    
    void approveAlert(Long id, AlertReviewRequest request);
    
    void banAlert(Long id, AlertReviewRequest request);
    
    ModerationDashboardResponse getDashboard();
}
