package com.example.travelez.backend.enhancement.model;

import com.example.travelez.backend.enhancement.dto.response.ItineraryEnhancementResponse;
import com.example.travelez.backend.users.model.User;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;

import java.time.LocalDateTime;

@Entity
@Table(name = "itinerary_enhancement_history")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItineraryEnhancementHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id", nullable = false)
    private User provider;

    @Column(name = "original_file_name")
    private String originalFileName;

    @Column(name = "provider_prompt")
    private String providerPrompt;

    @Type(JsonType.class)
    @Column(name = "analysis_result", columnDefinition = "jsonb")
    private ItineraryEnhancementResponse analysisResult;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
