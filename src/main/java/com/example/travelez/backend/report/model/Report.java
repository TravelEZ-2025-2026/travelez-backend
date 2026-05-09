package com.example.travelez.backend.report.model;

import com.example.travelez.backend.common.model.AuditableEntity;
import com.example.travelez.backend.posts.model.Posts;
import com.example.travelez.backend.report.model.enums.ReportReason;
import com.example.travelez.backend.report.model.enums.ReportStatus;
import com.example.travelez.backend.report.model.enums.ReportTargetType;
import com.example.travelez.backend.users.model.User;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "reports")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Report extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "reporter_id", nullable = false)
    private User reporter;

    @Column(name = "target_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private ReportTargetType targetType;

    @Column(name = "reason", nullable = false)
    @Enumerated(EnumType.STRING)
    private ReportReason reason;

    @Column(name = "reason_detail", nullable = true, columnDefinition = "TEXT")
    private String reasonDetail;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private ReportStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = true)
    private Posts post;
}
