package com.example.travelez.backend.reaction.service.impl;

import com.example.travelez.backend.common.api.CommonPage;
import com.example.travelez.backend.common.api.ResultCode;
import com.example.travelez.backend.common.exception.ApiException;
import com.example.travelez.backend.common.utils.SecurityUtils;
import com.example.travelez.backend.reaction.dto.request.ReactionToggleRequest;
import com.example.travelez.backend.reaction.dto.response.ReactorsResponse;
import com.example.travelez.backend.reaction.handler.ReactionTargetHandler;
import com.example.travelez.backend.reaction.mapper.ReactionMapper;
import com.example.travelez.backend.reaction.model.Reaction;
import com.example.travelez.backend.reaction.model.enums.ReactionTargetType;
import com.example.travelez.backend.reaction.repository.ReactionRepository;
import com.example.travelez.backend.reaction.service.ReactionService;
import com.example.travelez.backend.social.service.FollowService;
import com.example.travelez.backend.users.model.User;
import com.example.travelez.backend.users.service.impl.UserVectorTrackingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReactionServiceImpl implements ReactionService {

    private final Map<ReactionTargetType, ReactionTargetHandler> handlers;

    private final TransactionTemplate transactionTemplate;

    private final FollowService followService;

    private final ReactionRepository reactionRepository;

    private final ReactionMapper reactionMapper;

    private final UserVectorTrackingService userVectorTrackingService;

    @Override
    public void toggleReaction(ReactionToggleRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        ReactionTargetHandler handler = handlers.get(request.getTargetType());
        if (handler == null) {
            throw new IllegalArgumentException("Unsupported target type: " + request.getTargetType());
        }

        if (!handler.canInteract(request.getTargetId())) {
            throw new ApiException(ResultCode.FORBIDDEN, "You are not allowed to interact with this target");
        }

        boolean isLike = Boolean.TRUE.equals(transactionTemplate.execute(status -> {
            Optional<Reaction> existingOpt = handler.findExisting(userId, request.getTargetId());
            if (existingOpt.isPresent()) {
                Reaction existing = existingOpt.get();
                reactionRepository.delete(existing);

                return false;
            } else {
                Reaction reaction = Reaction.builder()
                        .user(User.builder().id(userId).build())
                        .build();
                handler.setTargetId(reaction, request.getTargetId());
                reactionRepository.save(reaction);

                return true;
            }
        }));

        userVectorTrackingService.handleReactionToggle(
                userId,
                request.getTargetId(),
                request.getTargetType(),
                isLike
        );
    }

    @Override
    public Map<Long, Long> getReactionCounts(ReactionTargetType targetType, List<Long> targetIds) {
        if (targetIds == null || targetIds.isEmpty()) return Map.of();
        ReactionTargetHandler handler = handlers.get(targetType);
        if (handler == null) {
            throw new IllegalArgumentException("Unsupported target type: " + targetType);
        }
        return handler.getReactionCountsAsMap(targetIds);
    }

    @Override
    public Set<Long> getUserReactedTargetIds(ReactionTargetType targetType, Long userId, List<Long> targetIds) {
        if (targetIds == null || targetIds.isEmpty()) return Set.of();
        ReactionTargetHandler handler = handlers.get(targetType);
        if (handler == null) {
            throw new IllegalArgumentException("Unsupported target type: " + targetType);
        }
        return handler.getUserReactedTargetIds(userId, targetIds);
    }

    @Override
    public CommonPage<ReactorsResponse> getReactors(ReactionTargetType targetType, Long targetId, Pageable pageable) {
        ReactionTargetHandler handler = handlers.get(targetType);
        if (handler == null) {
            throw new IllegalArgumentException("Unsupported target type: " + targetType);
        }
        Page<Reaction> page = handler.getReactorsPage(targetId, pageable);
        List<Reaction> reactions = page.getContent();

        List<Long> reactorIds = reactions.stream()
                .map(Reaction::getUser)
                .map(User::getId)
                .toList();
        Set<Long> reactorIdsFollowedByMe = followService.getUserIdsFollowedByMe(SecurityUtils.getCurrentUserId(), reactorIds);

        List<ReactorsResponse> reactorResponses = reactions.stream()
                .map(reaction -> reactionMapper.toReactorsResponse(reaction, reactorIdsFollowedByMe.contains(reaction.getUser().getId())))
                .toList();
        return new CommonPage<>(reactorResponses, page.getTotalPages(), page.getTotalElements(), pageable.getPageSize(), page.getNumber(), page.isEmpty());
    }
}
