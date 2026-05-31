package com.example.travelez.backend.moderation.dto.request;

import com.example.travelez.backend.common.dto.PaginationRequest;
import com.example.travelez.backend.moderation.model.enums.KeywordSeverity;
import com.example.travelez.backend.moderation.model.enums.ViolationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class BannedKeywordSearchRequest extends PaginationRequest {
    
    private ViolationType violationType;
    
    private KeywordSeverity severity;
    
    private Boolean isActive;
    
    private String keyword;
}
