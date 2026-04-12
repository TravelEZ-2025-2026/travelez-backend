package com.example.travelez.backend.poi.repository;

import com.example.travelez.backend.poi.model.PoiHour;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Set;

public interface PoiHourRepository extends JpaRepository<PoiHour, Long> {
    List<PoiHour> findByPoiIdIn(Set<Long> poiIds);
}
