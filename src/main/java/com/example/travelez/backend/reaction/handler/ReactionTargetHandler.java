package com.example.travelez.backend.reaction.handler;

import com.example.travelez.backend.reaction.model.Reaction;
import com.example.travelez.backend.reaction.model.enums.ReactionTargetType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public interface ReactionTargetHandler {
    // Trả về loại thực thể (POST, COMMENT, REVIEW...)
    ReactionTargetType getTargetType();

    // Kiểm tra thực thể có thể tương tác hay không
    boolean canInteract(Long targetId);

    List<Object[]> getRawReactionCounts(List<Long> targetIds);

    Set<Long> getUserReactedTargetIds(Long userId, List<Long> targetIds);

    // Tìm reaction hiện tại của user trên thực thể này
    Optional<Reaction> findExisting(Long userId, Long targetId);

    // Gán ID thực thể vào đúng cột Khóa ngoại trong Object Reaction
    void setTargetId(Reaction reaction, Long targetId);

    default Map<Long, Long> getReactionCountsAsMap(List<Long> targetIds) {

        if (targetIds == null || targetIds.isEmpty()) return Map.of();

        List<Object[]> rawReactionCounts = getRawReactionCounts(targetIds);

        return rawReactionCounts.stream().collect(Collectors.toMap(
                row -> (Long) row[0],
                row -> (Long) row[1]
        ));
    }

    Page<Reaction> getReactorsPage(Long targetId, Pageable pageable);

}
