package com.example.travelez.backend.itinerary.model;

import com.example.travelez.backend.poi.model.Poi;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "itinerary_activity")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItineraryActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    @Column(name = "time_of_day", length = 50)
    private String timeOfDay;

    @Column(length = 50)
    private String type;

    @Column(columnDefinition = "TEXT")
    private String note;

    @Column(name = "activity_cost", precision = 10, scale = 2)
    private BigDecimal activityCost;

    @Column(name = "transportation_cost", precision = 10, scale = 2)
    private BigDecimal transportationCost;

    @Column(name = "itinerary_date", nullable = false)
    private LocalDate itineraryDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "itinerary_id", nullable = false)
    private Itinerary itinerary;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "poi_id")
    private Poi poi;
}
