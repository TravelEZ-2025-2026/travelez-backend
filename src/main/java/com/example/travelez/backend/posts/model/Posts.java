package com.example.travelez.backend.posts.model;

import com.example.travelez.backend.common.model.AuditableEntity;
import com.example.travelez.backend.itinerary.model.Itinerary;
import com.example.travelez.backend.media.model.Media;
import com.example.travelez.backend.poi.model.Poi;
import com.example.travelez.backend.posts.model.enums.PostStatus;
import com.example.travelez.backend.users.model.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "posts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Posts extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title")
    private String title;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @ManyToOne
    @JoinColumn(name = "poi_id", nullable = true)
    private Poi poi;

    @Column(name = "topic_tag", nullable = true)
    private String topicTag;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private PostStatus status;

    @Column(name = "folder_id", nullable = true)
    private UUID folderId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "itinerary_id")
    private Itinerary itinerary;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @JoinTable(name = "media_posts",
            joinColumns = @JoinColumn(name = "post_id"),
            inverseJoinColumns = @JoinColumn(name = "media_id"))
    @OrderBy("id ASC")
    private List<Media> medias;

    public void addMedias(List<Media> medias) {
        this.medias.addAll(medias);
    }
}
