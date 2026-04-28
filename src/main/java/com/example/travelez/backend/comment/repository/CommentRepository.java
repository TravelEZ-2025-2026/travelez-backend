package com.example.travelez.backend.comment.repository;

import com.example.travelez.backend.comment.model.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    Optional<Comment> findByIdAndUserId(Long commentId, Long userId);

    Long countByPostId(Long postId);

    @Query("SELECT c.post.id, COUNT(c) FROM Comment c WHERE c.post.id in :ids AND c.deletedAt IS NULL GROUP BY c.post.id")
    List<Object[]> countByPostIds(List<Long> ids);

    @EntityGraph(attributePaths = {"user"})
    Page<Comment> findAllByPostIdAndParentCommentNull(Long postId, Pageable pageable);

    @Query("SELECT c.parentComment.id, COUNT(c) FROM Comment c " +
            "WHERE c.parentComment.id IN :parentIds AND c.deletedAt IS NULL " +
            "GROUP BY c.parentComment.id")
    List<Object[]> countRepliesForParents(@Param("parentIds") List<Long> parentIds);

    @EntityGraph(attributePaths = {"user"})
    Page<Comment> findAllByParentCommentId(Long parentCommentId, Pageable pageable);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "DELETE FROM comment WHERE id = :commentId", nativeQuery = true)
    void deleteCommentById(Long commentId);

    @EntityGraph(attributePaths = {"post"})
    @Query("SELECT c FROM Comment c JOIN FETCH c.post WHERE c.id = :commentId AND c.deletedAt IS NULL")
    Optional<Comment> findByIdWithPost(Long commentId);

    @Query("SELECT COUNT(c) FROM Comment c " +
            "WHERE c.createdAt >= :startDate " +
            "AND c.createdAt <= :endDate ")
    Long countComments(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}
