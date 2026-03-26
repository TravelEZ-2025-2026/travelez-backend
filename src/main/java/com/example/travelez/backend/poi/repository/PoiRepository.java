package com.example.travelez.backend.poi.repository;

import com.example.travelez.backend.poi.model.Place;
import com.example.travelez.backend.poi.model.Poi;
import com.example.travelez.backend.poi.model.enums.PlaceStatus;
import com.example.travelez.backend.poi.model.enums.PoiStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PoiRepository extends JpaRepository<Poi, Long>, JpaSpecificationExecutor<Poi> {
    Optional<Poi> findByIdAndSystemStatus(Long id, PoiStatus systemStatus);

    List<Poi> findByPlaceAndSystemStatusAndStatus(
            Place place,
            PoiStatus systemStatus,
            PlaceStatus status
    );

    @Query("SELECT poi.id, m FROM Poi poi JOIN poi.medias m WHERE poi.id IN :placeOfInterestIds")
    List<Object[]> findAllMediasByPoiIds(@Param("placeOfInterestIds") List<Long> placeOfInterestIds);
}
