package com.example.travelez.backend.moderation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlertReviewRequest {
    
    @NotBlank(message = "Admin note is required")
    @Size(max = 1000, message = "Admin note must not exceed 1000 characters")
    private String adminNote;
}
