package com.example.travelez.backend.poi.repository;

import com.example.travelez.backend.poi.model.Place;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface PlaceRepository extends JpaRepository<Place, Long>, JpaSpecificationExecutor<Place> {
    Optional<Place> findByCodename(String codeName);
}
