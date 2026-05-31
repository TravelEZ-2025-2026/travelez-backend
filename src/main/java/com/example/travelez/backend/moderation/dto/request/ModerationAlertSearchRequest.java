package com.example.travelez.backend.moderation.dto.request;

import com.example.travelez.backend.common.dto.PaginationRequest;
import com.example.travelez.backend.moderation.model.enums.AlertStatus;
import com.example.travelez.backend.moderation.model.enums.ModerationTargetType;
import com.example.travelez.backend.moderation.model.enums.ViolationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ModerationAlertSearchRequest extends PaginationRequest {
    
    private AlertStatus status;
    
    private ViolationType violationType;
    
    private ModerationTargetType targetType;
    
    private Long targetAuthorId;
    
    private LocalDateTime fromDate;
    
    private LocalDateTime toDate;
}
