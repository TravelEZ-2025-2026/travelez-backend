package com.example.travelez.backend.moderation.dto.internal;

import com.example.travelez.backend.moderation.model.enums.ModerationTargetType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModerationMessage implements Serializable {
    private Long targetId;
    private ModerationTargetType targetType;
}
