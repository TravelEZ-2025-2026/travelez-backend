package com.example.travelez.backend.reaction.handler;

import com.example.travelez.backend.common.utils.SecurityUtils;
import com.example.travelez.backend.posts.model.Posts;
import com.example.travelez.backend.posts.permission.PostsPermissionChecker;
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
public class PostReactionHandler implements ReactionTargetHandler {

    private final ReactionRepository reactionRepository;
    private final PostsPermissionChecker postsPermissionChecker;

    @Override
    public ReactionTargetType getTargetType() {
        return ReactionTargetType.POST;
    }

    @Override
    public boolean canInteract(Long targetId) {
        return postsPermissionChecker.canUserInteractPost(SecurityUtils.getCurrentUserId(), targetId);
    }

    @Override
    public List<Object[]> getRawReactionCounts(List<Long> targetIds) {
        return reactionRepository.countReactionsForPosts(targetIds);
    }

    @Override
    public Set<Long> getUserReactedTargetIds(Long userId, List<Long> targetIds) {
        if (userId == null) return Set.of();
        return reactionRepository.findReactedPostIdsByUser(userId, targetIds);
    }

    @Override
    public Optional<Reaction> findExisting(Long userId, Long targetId) {
        return reactionRepository.findByUserIdAndPostId(userId, targetId);
    }

    @Override
    public void setTargetId(Reaction reaction, Long targetId) {
        reaction.setPost(Posts.builder().id(targetId).build());
    }

    @Override
    public Page<Reaction> getReactorsPage(Long targetId, Pageable pageable) {
        return reactionRepository.findAllByPostId(targetId, pageable);
    }
}
