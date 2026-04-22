package com.example.travelez.backend.reaction.handler;

import com.example.travelez.backend.comment.model.Comment;
import com.example.travelez.backend.comment.permission.CommentPermissionChecker;
import com.example.travelez.backend.common.utils.SecurityUtils;
import com.example.travelez.backend.reaction.model.Reaction;
import com.example.travelez.backend.reaction.model.enums.ReactionTargetType;
import com.example.travelez.backend.reaction.repository.ReactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class CommentReactionHandler implements ReactionTargetHandler {

    private final ReactionRepository reactionRepository;
    private final CommentPermissionChecker commentPermissionChecker;

    @Override
    public ReactionTargetType getTargetType() {
        return ReactionTargetType.COMMENT;
    }

    @Override
    public boolean canInteract(Long targetId) {
        return commentPermissionChecker.canUserInteractComment(SecurityUtils.getCurrentUserId(), targetId);
    }

    @Override
    public List<Object[]> getRawReactionCounts(List<Long> targetIds) {
        return reactionRepository.countReactionsForComments(targetIds);
    }

    @Override
    public Set<Long> getUserReactedTargetIds(Long userId, List<Long> targetIds) {
        if (userId == null) return Set.of();
        return reactionRepository.findReactedCommentIdsByUser(userId, targetIds);
    }

    @Override
    public Optional<Reaction> findExisting(Long userId, Long targetId) {
        return reactionRepository.findByUserIdAndCommentId(userId, targetId);
    }

    @Override
    public void setTargetId(Reaction reaction, Long targetId) {
        reaction.setComment(Comment.builder().id(targetId).build());
    }

    @Override
    public Page<Reaction> getReactorsPage(Long targetId, Pageable pageable) {
        return reactionRepository.findAllByCommentId(targetId, pageable);
    }

}
