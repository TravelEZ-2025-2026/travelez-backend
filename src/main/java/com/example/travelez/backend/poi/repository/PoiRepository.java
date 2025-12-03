package com.example.travelez.backend.poi.repository;

import com.example.travelez.backend.poi.model.Poi;
import com.example.travelez.backend.poi.model.enums.PoiStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface PoiRepository extends JpaRepository<Poi, Long>, JpaSpecificationExecutor<Poi> {
    Optional<Poi> findByIdAndSystemStatus(Long id, PoiStatus systemStatus);
}
