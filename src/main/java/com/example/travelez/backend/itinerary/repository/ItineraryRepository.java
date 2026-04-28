package com.example.travelez.backend.itinerary.repository;

import com.example.travelez.backend.itinerary.model.Itinerary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ItineraryRepository extends JpaRepository<Itinerary, Long>, JpaSpecificationExecutor<Itinerary> {
    @Query("SELECT i FROM Itinerary i JOIN i.sharedUsers su WHERE su.userId = :userId")
    Page<Itinerary> findItinerariesSharedWithUser(@Param("userId") Long userId, Pageable pageable);
}
