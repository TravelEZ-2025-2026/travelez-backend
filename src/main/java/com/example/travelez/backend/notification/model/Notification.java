package com.example.travelez.backend.notification.model;

import com.example.travelez.backend.common.model.AuditableEntity;
import com.example.travelez.backend.notification.model.enums.NotificationType;
import com.example.travelez.backend.posts.model.Posts;
import com.example.travelez.backend.users.model.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "notification")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "type", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private NotificationType notificationType;

    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private boolean isRead = false;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Posts posts;
}
