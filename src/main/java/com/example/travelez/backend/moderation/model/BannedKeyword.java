package com.example.travelez.backend.moderation.model;

import com.example.travelez.backend.common.model.AuditableEntity;
import com.example.travelez.backend.moderation.model.enums.KeywordSeverity;
import com.example.travelez.backend.moderation.model.enums.ViolationType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "banned_keywords")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class BannedKeyword extends AuditableEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "keyword", nullable = false, unique = true)
    private String keyword;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "violation_type", nullable = false)
    private ViolationType violationType;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false)
    private KeywordSeverity severity;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
}
