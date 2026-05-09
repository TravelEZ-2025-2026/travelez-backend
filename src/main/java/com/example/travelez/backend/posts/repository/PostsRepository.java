package com.example.travelez.backend.posts.repository;

import com.example.travelez.backend.posts.model.Posts;
import com.example.travelez.backend.posts.model.enums.PostStatus;
import com.example.travelez.backend.posts.repository.projection.TopPoiProjection;
import com.example.travelez.backend.posts.repository.projection.TopTagProjection;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PostsRepository extends JpaRepository<Posts, Long>, JpaSpecificationExecutor<Posts> {

    @EntityGraph(attributePaths = {"user"})
    Optional<Posts> findByIdAndUserId(Long postId, Long userId);

//     @Query("SELECT p FROM Posts p WHERE (p.user.id = :id OR p.user.id in (SELECT f.following.id FROM Follow f WHERE f.follower.id = :id)) AND p.status = 'PUBLIC' ORDER BY p.id DESC ")
//     Slice<Posts> findFriendPostsFirstPage(@Param("id") Long userId, Pageable pageable);

//     @Query("SELECT p FROM Posts p WHERE (p.user.id = :id OR p.user.id in (SELECT f.following.id FROM Follow f WHERE f.follower.id = :id)) AND p.id < :lastPostId  AND p.status = 'PUBLIC' ORDER BY p.id DESC ")
//     Slice<Posts> findFriendPostsNextPage(@Param("id") Long userId, @Param("lastPostId") Long lastPostId, Pageable pageable);

//     @Query("SELECT p FROM Posts p " +
//             "WHERE p.user.id <> :id " +
//             "AND p.user.id NOT IN (SELECT f.following.id FROM Follow f WHERE f.follower.id = :id) " +
//             "AND p.status = 'PUBLISHED' " +
//             "ORDER BY p.id DESC ")
//     Slice<Posts> findSuggestPostsFirstPage(@Param("id") Long userId, Pageable pageable);

//     @Query("SELECT p FROM Posts p " +
//             "WHERE p.user.id <> :id " +
//             "AND p.user.id NOT IN (SELECT f.following.id FROM Follow f WHERE f.follower.id = :id) " +
//             "AND p.status = 'PUBLISHED' " +
//             "AND p.id < :lastPostId " +
//             "ORDER BY p.id DESC ")
//     Slice<Posts> findSuggestedPostsNextPage(@Param("id") Long userId, @Param("lastPostId") Long lastPostId, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "poi"})
    @Query("SELECT p FROM Posts p " +
            "WHERE p.status = 'PUBLISHED' " +
            "ORDER BY p.id DESC")
    Slice<Posts> findAllPostsFirstPage(Pageable pageable);

    @Query("SELECT p FROM Posts p " +
            "WHERE p.status = 'PUBLISHED' " +
            "AND p.id < :lastPostId " +
            "ORDER BY p.id DESC")
    Slice<Posts> findAllPostsNextPage(@Param("lastPostId") Long lastPostId, Pageable pageable);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "DELETE FROM posts WHERE id = :postId", nativeQuery = true)
    void deletePostById(Long postId);

    boolean existsByIdAndUserId(Long postId, Long userId);

    boolean existsByIdAndStatus(Long postId, PostStatus status);

    @EntityGraph(attributePaths = {"user", "poi", "poi.ward", "poi.place"})
    Page<Posts> findAll(Specification<Posts> specification, Pageable pageable);

    // Admin 
    // --- 1. THỐNG KÊ TỔNG QUAN ---
    @Query("SELECT COUNT(p) " +
            "FROM Posts p " +
            "WHERE p.status = 'PUBLISHED' " +
            "AND p.createdAt >= :startDate " +
            "AND p.createdAt <= :endDate ")
    Long countPublicPosts(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // 2. THỐNG KÊ TOP POI (Của bài PUBLISHED)
    @Query("SELECT p.poi.id as id, p.poi.name as name, COUNT(p) as countPost FROM Posts p " +
            "WHERE p.poi IS NOT NULL AND p.status = 'PUBLISHED' " +
            "AND p.createdAt >= :startDate " +
            "AND p.createdAt <= :endDate " +
            "GROUP BY p.poi.id, p.poi.name " +
            "ORDER BY COUNT(p) DESC")
    List<TopPoiProjection> getTopPois(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, Pageable pageable);

    @Query("SELECT p.topicTag as topicTag, COUNT(p) as countPost FROM Posts p " +
            "WHERE p.topicTag IS NOT NULL AND p.topicTag <> '' " + // Loại bỏ tag rỗng
            "AND p.status = 'PUBLISHED' " +
            "AND p.createdAt >= :startDate " +
            "AND p.createdAt <= :endDate " +
            "GROUP BY p.topicTag " +
            "ORDER BY COUNT(p) DESC")
    List<TopTagProjection> getTopTags(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, Pageable pageable);

    @EntityGraph(attributePaths = {"user"})
    @Query("SELECT p FROM Posts p WHERE p.id = :postId")
    Optional<Posts> findByPostId(@Param("postId") Long postId);
}
