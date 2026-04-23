package com.example.travelez.backend.poi.model;

import com.example.travelez.backend.common.model.AuditableEntity;
import com.example.travelez.backend.media.model.Media;
import com.example.travelez.backend.poi.model.enums.PlaceStatus;
import com.example.travelez.backend.poi.model.enums.PoiStatus;
import com.example.travelez.backend.poi.model.enums.PoiType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "place_of_interest")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SQLDelete(sql = "UPDATE place_of_interest SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Poi extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "name", nullable = false, columnDefinition = "TEXT")
    private String name;

    @Column(name = "poi_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private PoiType poiType;

    @Column(name = "poi_type_detail")
    private String poiTypeDetail;

    @Column(name = "system_status", nullable = false)
    @Enumerated(EnumType.STRING)
    private PoiStatus systemStatus;

    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    @Column(name = "website", columnDefinition = "TEXT")
    private String website;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "rating")
    private Double rating;

    @Column(name = "review_count")
    private Integer reviewCount;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private PlaceStatus status;

    @Column(name = "google_maps_url", columnDefinition = "TEXT")
    private String googleMapsUrl;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "semantic_text", columnDefinition = "TEXT")
    private String semanticText;

    @Column(name = "google_place_id", unique = true)
    private String googlePlaceId;

    @Column(name = "opening_hour", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private List<OpeningHours> openingHour;

    @Column(name = "reviews_distribution", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private ReviewDistributions reviewsDistribution;

    @Column(name = "additional_info", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Boolean> additionalInfo;

    @ManyToOne
    @JoinColumn(name = "place_id")
    private Place place;

    @ManyToOne
    @JoinColumn(name = "ward_id")
    private Ward ward;

    // @OneToMany(fetch = FetchType.LAZY, mappedBy = "poi", cascade =
    // CascadeType.ALL, orphanRemoval = true)
    // private List<Review> reviews;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @JoinTable(name = "media_poi", joinColumns = @JoinColumn(name = "place_of_interest_id"), inverseJoinColumns = @JoinColumn(name = "media_id"))
    private List<Media> medias;

    @Column(name = "deleted_at", nullable = true)
    private LocalDateTime deletedAt;
}
