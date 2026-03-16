package com.example.travelez.backend.posts.repository;

import com.example.travelez.backend.posts.model.Posts;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PostsRepository extends JpaRepository<Posts, Long>, JpaSpecificationExecutor<Posts> {

    Optional<Posts> findByIdAndUserId(Long postId, Long userId);

    @Query("SELECT p FROM Posts p WHERE (p.user.id = :id OR p.user.id in (SELECT f.following.id FROM Follow f WHERE f.follower.id = :id)) AND p.status = 'PUBLIC' ORDER BY p.id DESC ")
    Slice<Posts> findFriendPostsFirstPage(@Param("id") Long userId, Pageable pageable);

    @Query("SELECT p FROM Posts p WHERE (p.user.id = :id OR p.user.id in (SELECT f.following.id FROM Follow f WHERE f.follower.id = :id)) AND p.id < :lastPostId  AND p.status = 'PUBLIC' ORDER BY p.id DESC ")
    Slice<Posts> findFriendPostsNextPage(@Param("id") Long userId, @Param("lastPostId") Long lastPostId, Pageable pageable);

    @Query("SELECT p FROM Posts p " +
            "WHERE p.user.id <> :id " +
            "AND p.user.id NOT IN (SELECT f.following.id FROM Follow f WHERE f.follower.id = :id) " +
            "AND p.status = 'PUBLISHED' " +
            "ORDER BY p.id DESC ")
    Slice<Posts> findSuggestPostsFirstPage(@Param("id") Long userId, Pageable pageable);

    @Query("SELECT p FROM Posts p " +
            "WHERE p.user.id <> :id " +
            "AND p.user.id NOT IN (SELECT f.following.id FROM Follow f WHERE f.follower.id = :id) " +
            "AND p.status = 'PUBLISHED' " +
            "AND p.id < :lastPostId " +
            "ORDER BY p.id DESC ")
    Slice<Posts> findSuggestedPostsNextPage(@Param("id") Long userId, @Param("lastPostId") Long lastPostId, Pageable pageable);
}
