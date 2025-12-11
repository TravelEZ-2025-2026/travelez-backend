package com.example.travelez.backend.review.model;

import com.example.travelez.backend.common.model.AuditableEntity;
import com.example.travelez.backend.media.model.Media;
import com.example.travelez.backend.poi.model.Poi;
import com.example.travelez.backend.review.model.enums.ReviewStatus;
import com.example.travelez.backend.users.model.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.util.List;

@Entity
@Table(name = "review")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "google_review_id", unique = true)
    private String google_review_id;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private ReviewStatus status;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "rating")
    private Double rating;

    @Column(name = "external_name")
    private String externalName;

    @Column(name = "external_avt", columnDefinition = "TEXT")
    private String externalAvt;

    @Column(name = "is_crawled", nullable = false)
    private Boolean isCrawled;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_of_interest_id")
    private Poi poi;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @JoinTable(name = "media_review", joinColumns = @JoinColumn(name = "review_id"), inverseJoinColumns = @JoinColumn(name = "media_id"))
    private List<Media> medias;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "traveler_id")
    private User traveler;
}
