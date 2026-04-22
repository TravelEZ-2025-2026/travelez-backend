package com.example.travelez.backend.reaction.service;

import com.example.travelez.backend.common.api.CommonPage;
import com.example.travelez.backend.reaction.dto.request.ReactionToggleRequest;
import com.example.travelez.backend.reaction.dto.response.ReactorsResponse;
import com.example.travelez.backend.reaction.model.enums.ReactionTargetType;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ReactionService {

    void toggleReaction(ReactionToggleRequest request);

    Map<Long, Long> getReactionCounts(ReactionTargetType targetType, List<Long> targetIds);

    Set<Long> getUserReactedTargetIds(ReactionTargetType targetType, Long userId, List<Long> targetIds);

    CommonPage<ReactorsResponse> getReactors(ReactionTargetType targetType, Long targetId, Pageable pageable);

}
