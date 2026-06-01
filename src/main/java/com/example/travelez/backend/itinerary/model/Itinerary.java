package com.example.travelez.backend.itinerary.model;

import com.example.travelez.backend.common.model.AuditableEntity;
import com.example.travelez.backend.itinerary.model.enums.ItineraryStatus;
import com.example.travelez.backend.users.model.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
    private String userNotes;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(precision = 12, scale = 2)
    private BigDecimal budget;

    // Estimated budget that AI answers
    @Column(name = "estimated_total_price", precision = 12, scale = 2)
    private BigDecimal estimatedTotalPrice;

    @Column(name = "estimated_transportation_price", precision = 12, scale = 2)
    private BigDecimal estimatedTransportationPrice;

    @Column(name = "estimated_activity_price", precision = 12, scale = 2)
    private BigDecimal estimatedActivityPrice;

    @Column(name = "estimated_food_and_drink_price", precision = 12, scale = 2)
    private BigDecimal estimatedFoodAndDrinkPrice;

    @Column(name = "estimated_accommodation_price", precision = 12, scale = 2)
    private BigDecimal estimatedAccommodationPrice;

    @Column(name = "estimated_currency", length = 10)
    private String estimatedCurrency;

    @Column(name = "has_kids")
    private Boolean hasKids;

    @Column(name = "has_pets")
    private Boolean hasPets;

    @Column(name = "companion")
    private String companion;

    @Column(columnDefinition = "TEXT")
    private String objectives;

    @Column(name = "objectives_vector", columnDefinition = "text")
    private String objectivesVector;

    @Column(name = "is_public", nullable = false, columnDefinition = "boolean default false")
    private Boolean isPublic = false;

    @Column(name = "calendar_synced_at")
    private LocalDateTime calendarSyncedAt;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ItineraryStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "traveler_id", nullable = false)
    private User traveler;

    // Quan hệ với Activity
    @OneToMany(mappedBy = "itinerary", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItineraryActivity> activities;

    @OneToMany(mappedBy = "itinerary", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItinerarySharedUser> sharedUsers = new ArrayList<>();

}
