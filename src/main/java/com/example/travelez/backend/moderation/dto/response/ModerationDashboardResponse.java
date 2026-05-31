package com.example.travelez.backend.moderation.dto.response;

import com.example.travelez.backend.moderation.model.enums.ModerationTargetType;
import com.example.travelez.backend.moderation.model.enums.ViolationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModerationDashboardResponse {
    private Long totalAlerts;
    private Long pendingAlerts;
    private Long approvedAlerts;
    private Long bannedAlerts;
    private Long totalBannedKeywords;
    private Long activeKeywords;
    
    private Map<ViolationType, Long> violationTypeStats;
    private Map<ModerationTargetType, Long> targetTypeStats;
}
