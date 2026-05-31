package com.example.travelez.backend.moderation.dto.response;

import com.example.travelez.backend.moderation.model.enums.AlertStatus;
import com.example.travelez.backend.moderation.model.enums.ModerationTargetType;
import com.example.travelez.backend.moderation.model.enums.ViolationType;
import com.example.travelez.backend.users.dto.response.UserSummaryResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ModerationAlertResponse {
    private Long id;
    private Long targetId;
    private ModerationTargetType targetType;
    private ViolationType violationType;
    private Double confidenceScore;
    private String reason;
    private AlertStatus status;
    private LocalDateTime createdAt;
    
    // Target content info
    private String targetTitle;
    private String targetContent;
    private String targetAuthorName;
    private Long targetAuthorId;
    
    // Review info
    private UserSummaryResponse reviewedBy;
    private LocalDateTime reviewedAt;
    private String adminNote;
}
