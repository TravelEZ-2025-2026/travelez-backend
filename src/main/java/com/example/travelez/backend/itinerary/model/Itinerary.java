package com.example.travelez.backend.itinerary.model;

import com.example.travelez.backend.common.model.AuditableEntity;
import com.example.travelez.backend.users.model.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "itinerary")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Itinerary extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(name = "styles", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private List<String> styles;

    @Column(name = "destination_cities", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private List<String> destinationCities;

    @Column(name = "user_notes", columnDefinition = "TEXT")
    private String userNotes; // Lưu specialNotes

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(precision = 12, scale = 2)
    private BigDecimal budget;

    @Column(name = "has_kids")
    private Boolean hasKids;

    @Column(name = "has_pets")
    private Boolean hasPets;

    @Column(name = "companion")
    private String companion;

    @Column(columnDefinition = "TEXT")
    private String objectives; // Mapping từ "reasoningSummary"

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ItineraryStatus status;

    // --- BỔ SUNG QUAN HỆ VỚI USER (TRAVELER) ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "traveler_id", nullable = false) // Khớp với ảnh schema 2
    private User traveler;

    // Quan hệ với Activity
    @OneToMany(mappedBy = "itinerary", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItineraryActivity> activities;

    public enum ItineraryStatus {
        PLANNING, ONGOING, COMPLETED, CANCELLED
    }
}
