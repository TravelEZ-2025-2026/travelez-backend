package com.example.travelez.backend.report.repository;

import com.example.travelez.backend.report.model.Report;
import com.example.travelez.backend.report.model.enums.ReportStatus;
import com.example.travelez.backend.report.repository.projection.ReportedPostProjection;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReportRepository extends JpaRepository<Report, Long> {
    @Modifying
    @Query("UPDATE Report r SET r.status = :newStatus WHERE r.post.id = :postId AND r.status = :oldStatus")
    void updateStatusByPostId(@Param("postId") Long postId, @Param("newStatus") ReportStatus newStatus, @Param("oldStatus") ReportStatus oldStatus);

    @EntityGraph(attributePaths = {"reporter"})
    List<Report> findByPostIdAndStatus(Long postId, ReportStatus status);

    // Get posts with at least 1 PENDING report (default)
    @Query("""
        SELECT p.id AS postId,
               p.title AS title,
               p.content AS content,
               u.id AS authorId,
               u.fullName AS authorName,
               COUNT(r.id) AS reportCount,
               SUM(CASE WHEN r.status = 'PENDING' THEN 1 ELSE 0 END) AS pendingReportCount,
               MIN(r.createdAt) AS firstReportedAt,
               MAX(r.createdAt) AS latestReportedAt,
               p.status AS postStatus
        FROM Report r
        JOIN r.post p
        JOIN p.user u
        GROUP BY p.id, p.title, p.content, u.id, u.fullName, p.status
        HAVING SUM(CASE WHEN r.status = 'PENDING' THEN 1 ELSE 0 END) >= 1
        """)
    Page<ReportedPostProjection> findPostsWithPendingReports(Pageable pageable);

    // Get all reported posts (regardless of report status)
    @Query("""
        SELECT p.id AS postId,
               p.title AS title,
               p.content AS content,
               u.id AS authorId,
               u.fullName AS authorName,
               COUNT(r.id) AS reportCount,
               SUM(CASE WHEN r.status = 'PENDING' THEN 1 ELSE 0 END) AS pendingReportCount,
               MIN(r.createdAt) AS firstReportedAt,
               MAX(r.createdAt) AS latestReportedAt,
               p.status AS postStatus
        FROM Report r
        JOIN r.post p
        JOIN p.user u
        GROUP BY p.id, p.title, p.content, u.id, u.fullName, p.status
        HAVING COUNT(r.id) >= 1
        """)
    Page<ReportedPostProjection> findAllReportedPosts(Pageable pageable);

    @EntityGraph(attributePaths = {"reporter"})
    Page<Report> findByPostId(Long postId, Pageable pageable);

    @EntityGraph(attributePaths = {"reporter"})
    Page<Report> findByPostIdAndStatus(Long postId, ReportStatus status, Pageable pageable);

    Long countByPostId(Long postId);

    Long countByPostIdAndStatus(Long postId, ReportStatus status);

    Long countByStatus(ReportStatus status);
}
