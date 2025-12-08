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

    // Mapping từ: activityName + " tại " + locationName
    @Column(columnDefinition = "TEXT")
    private String description;

    // Mapping từ timeSlot (lấy giờ bắt đầu)
    @Column(name = "start_time")
    private LocalTime startTime;

    // Mapping logic: Sáng/Chiều/Tối
    @Column(name = "time_of_day", length = 50)
    private String timeOfDay;

    // Mapping từ activityType (ATTRACTION, FOOD...)
    @Column(length = 50)
    private String type;

    // Mapping từ notes
    @Column(columnDefinition = "TEXT")
    private String note;

    // Các cột chi phí (để 0 hoặc null ban đầu)
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
    @JoinColumn(name = "poi_id") // Có thể null nếu là Custom Activity (Khách sạn)
    private Poi poi;
}
