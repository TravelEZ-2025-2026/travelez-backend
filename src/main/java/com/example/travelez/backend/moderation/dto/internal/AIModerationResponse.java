package com.example.travelez.backend.moderation.dto.internal;

import com.example.travelez.backend.moderation.model.enums.ViolationType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIModerationResponse {
    
    @JsonProperty("isSafe")
    private boolean isSafe;
    
    @JsonProperty("violationType")
    private String violationType;
    
    @JsonProperty("confidenceScore")
    private Double confidenceScore;
    
    @JsonProperty("reason")
    private String reason;
    
    public ViolationType getViolationTypeEnum() {
        if (violationType == null || violationType.equals("NONE")) {
            return null;
        }
        try {
            return ViolationType.valueOf(violationType);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
