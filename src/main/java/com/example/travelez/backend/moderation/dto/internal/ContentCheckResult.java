package com.example.travelez.backend.moderation.dto.internal;

import com.example.travelez.backend.moderation.model.enums.KeywordSeverity;
import com.example.travelez.backend.moderation.model.enums.ViolationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContentCheckResult {
    private boolean isSafe;
    private ViolationType violationType;
    private String matchedKeyword;
    private KeywordSeverity severity;
    private String reason;
}
