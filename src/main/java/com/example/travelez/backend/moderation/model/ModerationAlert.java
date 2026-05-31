package com.example.travelez.backend.moderation.model;

import com.example.travelez.backend.common.model.AuditableEntity;
import com.example.travelez.backend.moderation.model.enums.AlertStatus;
import com.example.travelez.backend.moderation.model.enums.ModerationTargetType;
import com.example.travelez.backend.moderation.model.enums.ViolationType;
import com.example.travelez.backend.users.model.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Entity
@Table(name = "moderation_alerts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ModerationAlert extends AuditableEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "target_id", nullable = false)
    private Long targetId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false)
    private ModerationTargetType targetType;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "violation_type", nullable = false)
    private ViolationType violationType;
    
    @Column(name = "confidence_score", nullable = false)
    private Double confidenceScore;
    
    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private AlertStatus status = AlertStatus.PENDING;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private User reviewedBy;
    
    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;
    
    @Column(name = "admin_note", columnDefinition = "TEXT")
    private String adminNote;
}
