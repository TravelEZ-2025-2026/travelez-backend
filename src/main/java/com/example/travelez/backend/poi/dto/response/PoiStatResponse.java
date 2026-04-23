package com.example.travelez.backend.poi.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Map;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class PoiStatResponse {
    //    Thống kê hệ thống
    private Long totalPoi;
    private Long activePoi;
    private Long bannedPoi;
    //    Thống kê nghiệp vụ
    private Long operationalPoi;
    private Long closedPoi;

    //    Phân bố loại hình
    private Map<String, Long> poisByType;

    //    Thống kê tương tác
    private Long totalReviews;
    private Double averageRating;
}
