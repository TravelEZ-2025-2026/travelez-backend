package com.example.travelez.backend.review.model;

import java.util.List;

import com.example.travelez.backend.common.model.AuditableEntity;
import com.example.travelez.backend.media.model.Media;
import com.example.travelez.backend.poi.model.Poi;
import com.example.travelez.backend.review.model.enums.ReviewStatus;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

@Entity
@Table(name = "review")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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

    @Column(name = "content")
    private String content;

    @Column(name = "rating")
    private Double rating;

    @Column(name = "external_name")
    private String externalName;

    @Column(name = "external_avt")
    private String externalAvt;

    @Column(name = "is_crawled", nullable = false)
    private Boolean isCrawled;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_of_interest_id")
    private Poi poi;

    @ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE }, fetch = FetchType.LAZY)
    @JoinTable(name = "media_review", joinColumns = @JoinColumn(name = "review_id"), inverseJoinColumns = @JoinColumn(name = "media_id"))
    private List<Media> medias;
}
