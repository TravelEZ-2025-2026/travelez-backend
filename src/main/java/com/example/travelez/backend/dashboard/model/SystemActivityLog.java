package com.example.travelez.backend.dashboard.model;

import com.example.travelez.backend.dashboard.model.enums.ActivityCategory;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "system_activity_logs")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private ActivityCategory category;

    private String description;
    private String status;
    private LocalDateTime createdAt;
}