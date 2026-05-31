package com.example.travelez.backend.moderation.dto.internal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIModerationRequest {
    private String text;
    private List<String> imageUrls;
}
