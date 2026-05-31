package com.example.travelez.backend.moderation.dto.request;

import com.example.travelez.backend.moderation.model.enums.KeywordSeverity;
import com.example.travelez.backend.moderation.model.enums.ViolationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BannedKeywordCreateRequest {
    
    @NotBlank(message = "Keyword is required")
    @Size(min = 1, max = 255, message = "Keyword must be between 1 and 255 characters")
    private String keyword;
    
    @NotNull(message = "Violation type is required")
    private ViolationType violationType;
    
    @NotNull(message = "Severity is required")
    private KeywordSeverity severity;
    
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;
}
