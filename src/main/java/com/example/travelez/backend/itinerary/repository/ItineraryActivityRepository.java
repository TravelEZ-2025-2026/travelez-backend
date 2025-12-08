package com.example.travelez.backend.itinerary.repository;

import com.example.travelez.backend.itinerary.model.ItineraryActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItineraryActivityRepository extends JpaRepository<ItineraryActivity, Long> {

    @Query("SELECT a FROM ItineraryActivity a LEFT JOIN FETCH a.poi WHERE a.itinerary.id = :itineraryId ORDER BY a.itineraryDate ASC, a.startTime ASC")
    List<ItineraryActivity> findByItineraryIdOrderByItineraryDateAscStartTimeAsc(@Param("itineraryId") Long itineraryId);
}
