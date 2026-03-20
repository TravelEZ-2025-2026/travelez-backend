package com.example.travelez.backend.comment.repository;

import com.example.travelez.backend.comment.model.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    Optional<Comment> findByIdAndUserId(Long commentId, Long userId);

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
}
