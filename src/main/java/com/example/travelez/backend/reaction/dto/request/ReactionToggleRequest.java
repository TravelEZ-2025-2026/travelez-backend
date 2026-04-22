package com.example.travelez.backend.reaction.dto.request;

import com.example.travelez.backend.reaction.model.enums.ReactionTargetType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReactionToggleRequest {
    @NotNull
    @Enumerated(EnumType.STRING)
    private ReactionTargetType targetType;

    @NotNull
    private Long targetId;
}
