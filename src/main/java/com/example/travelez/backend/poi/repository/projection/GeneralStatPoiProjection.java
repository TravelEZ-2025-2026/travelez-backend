package com.example.travelez.backend.poi.repository.projection;

public interface GeneralStatPoiProjection {
    Long getTotal();

    Long getActiveCount();

    Long getBannedCount();

    Long getOperationalCount();

    Long getClosedCount();
    
    Long getTotalReview();

    Double getAverageRating();
}
