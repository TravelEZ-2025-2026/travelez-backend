package com.example.travelez.backend.users.model;

import com.example.travelez.backend.common.model.AuditableEntity;
import com.example.travelez.backend.media.model.Media;
import com.example.travelez.backend.users.model.enums.AuthProvider;
import com.example.travelez.backend.users.model.enums.GenderType;
import com.example.travelez.backend.users.model.enums.RoleType;
import com.example.travelez.backend.users.model.enums.UserStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "role", discriminatorType = DiscriminatorType.STRING)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class User extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "username", nullable = false, unique = true)
    private String username;

    @Column(name = "password")
    private String password;

    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "gender")
    @Enumerated(EnumType.STRING)
    private GenderType gender;

    @Column(name = "dob")
    private LocalDateTime dob;

    @Column(name = "status", nullable = false)
    @Builder.Default
    @Enumerated(EnumType.STRING)
    private UserStatus status = UserStatus.ACTIVE;

    @Column(name = "role", nullable = false, updatable = false, insertable = false)
    @Enumerated(EnumType.STRING)
    private RoleType role;

    @Column(name = "google_id")
    private String googleId;

    @Column(name = "follower_count", nullable = false)
    private Long followerCount;

    @Column(name = "following_count", nullable = false)
    private Long followingCount;

    @Column(name = "auth_provider")
    @Enumerated(EnumType.STRING)
    private AuthProvider authProvider;

    @OneToOne
    @JoinColumn(name = "avatar_id")
    private Media avatar;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cover_id")
    private Media cover;
}
