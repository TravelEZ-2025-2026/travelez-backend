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
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "profile_vector", columnDefinition = "text")
    private String profileVector;
}
