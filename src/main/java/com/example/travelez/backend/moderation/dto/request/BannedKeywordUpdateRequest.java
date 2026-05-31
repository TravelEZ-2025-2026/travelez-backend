package com.example.travelez.backend.moderation.dto.request;

import com.example.travelez.backend.moderation.model.enums.KeywordSeverity;
import com.example.travelez.backend.moderation.model.enums.ViolationType;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BannedKeywordUpdateRequest {
    
    private ViolationType violationType;
    
    private KeywordSeverity severity;
    
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;
}
