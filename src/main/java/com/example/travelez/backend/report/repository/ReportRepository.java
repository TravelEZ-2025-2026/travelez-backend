package com.example.travelez.backend.report.repository;

import com.example.travelez.backend.report.model.Report;
import com.example.travelez.backend.report.model.enums.ReportStatus;

import java.util.List;

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
}
