package com.example.travelez.backend.reaction.repository;

import com.example.travelez.backend.reaction.model.Reaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ReactionRepository extends JpaRepository<Reaction, Long> {
    @Query("SELECT r FROM Reaction r JOIN r.post p ON r.post.id = p.id WHERE r.user.id = :userId AND r.post.id = :postId")
    Optional<Reaction> findByUserIdAndPostId(@Param("userId") Long userId, @Param("postId") Long postId);

    @Query("SELECT r FROM Reaction r JOIN r.comment c ON r.comment.id = c.id WHERE r.user.id = :userId AND r.comment.id = :commentId AND c.deletedAt IS NULL")
    Optional<Reaction> findByUserIdAndCommentId(@Param("userId") Long userId, @Param("commentId") Long commentId);

    @Query("SELECT p.id, COUNT(r) FROM Reaction r JOIN r.post p ON r.post.id = p.id WHERE r.post.id IN :postIds GROUP BY p.id")
    List<Object[]> countReactionsForPosts(@Param("postIds") List<Long> postIds);

    @Query("SELECT c.id, COUNT(r) FROM Reaction r JOIN r.comment c ON r.comment.id = c.id WHERE r.comment.id IN :commentIds GROUP BY c.id")
    List<Object[]> countReactionsForComments(@Param("commentIds") List<Long> commentIds);

    @Query("SELECT r.post.id FROM Reaction r WHERE r.user.id = :userId AND r.post.id IN :postIds")
    Set<Long> findReactedPostIdsByUser(@Param("userId") Long userId, @Param("postIds") List<Long> postIds);

    @Query("SELECT r.comment.id FROM Reaction r WHERE r.user.id = :userId AND r.comment.id IN :commentIds")
    Set<Long> findReactedCommentIdsByUser(@Param("userId") Long userId, @Param("commentIds") List<Long> commentIds);

    @EntityGraph(attributePaths = {"user", "user.avatar"})
    @Query("SELECT r FROM Reaction r WHERE r.post.id = :postId")
    Page<Reaction> findAllByPostId(@Param("postId") Long postId, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "user.avatar"})
    @Query("SELECT r FROM Reaction r WHERE r.comment.id = :commentId")
    Page<Reaction> findAllByCommentId(@Param("commentId") Long commentId, Pageable pageable);
}
