package com.example.travelez.backend.users.model;

import java.time.LocalDateTime;

import com.example.travelez.backend.common.model.AuditableEntity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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

    @Column(name = "avatar")
    private String avatar;

    @Column(name = "role", nullable = false)
    @Enumerated(EnumType.STRING)
    private RoleType role;

    public enum GenderType {
        MALE,
        FEMALE,
        OTHER
    }

    public enum RoleType {
        TRAVELER,
        PROVIDER,
        ADMIN
    }

    public enum UserStatus {
        ACTIVE,
        BANNED
    }
}
