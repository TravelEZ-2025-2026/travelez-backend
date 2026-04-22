package com.example.travelez.backend.poi.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;

@Entity
@Table(name = "poi_hours")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PoiHour {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "place_of_interest_id", nullable = false)
    private Long poiId;

    // Chú ý: Ở database bạn đang dùng kiểu số Int cho thứ (VD: T2 = 2, CN = 8 theo chuẩn postgres của bạn)
    @Column(name = "day_of_week", nullable = false)
    private Integer dayOfWeek;

    @Column(name = "open_time", nullable = false)
    private LocalTime openTime;

    @Column(name = "close_time", nullable = false)
    private LocalTime closeTime;
}
