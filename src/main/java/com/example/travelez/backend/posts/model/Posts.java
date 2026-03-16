package com.example.travelez.backend.posts.model;

import com.example.travelez.backend.common.model.AuditableEntity;
import com.example.travelez.backend.media.model.Media;
import com.example.travelez.backend.posts.model.enums.PostStatus;
import com.example.travelez.backend.users.model.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "posts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Posts extends AuditableEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title")
    private String title;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private PostStatus status;

    @Column(name = "folder_id", nullable = true)
    private UUID folderId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

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
