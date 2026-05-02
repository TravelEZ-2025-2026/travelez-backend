package com.example.travelez.backend.users.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_profile_vector")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileVector {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @Column(name = "profile_vector", columnDefinition = "text")
    private String profileVector;
}
