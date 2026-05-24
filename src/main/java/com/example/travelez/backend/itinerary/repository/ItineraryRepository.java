package com.example.travelez.backend.itinerary.repository;

import com.example.travelez.backend.itinerary.model.Itinerary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ItineraryRepository extends JpaRepository<Itinerary, Long>, JpaSpecificationExecutor<Itinerary> {
    @Query("SELECT i FROM Itinerary i JOIN i.sharedUsers su WHERE su.userId = :userId")
    Page<Itinerary> findItinerariesSharedWithUser(@Param("userId") Long userId, Pageable pageable);

    @Modifying
    @Query(value = "UPDATE itinerary SET objectives_vector = cast(:vectorStr as vector) WHERE id = :id", nativeQuery = true)
    void updateObjectivesVector(@Param("id") Long id, @Param("vectorStr") String vectorStr);

    @Query(
            value = "SELECT * FROM itinerary " +
                    "WHERE is_public = true " +
                    "ORDER BY objectives_vector <=> cast(:vectorStr as vector)",
            countQuery = "SELECT COUNT(*) FROM itinerary WHERE is_public = true",
            nativeQuery = true
    )
    Page<Itinerary> searchPublicItinerariesByVector(@Param("vectorStr") String vectorStr, Pageable pageable);

    @Query("SELECT i FROM Itinerary i WHERE i.traveler.id = :userId AND i.isPublic = true")
    Page<Itinerary> findPublicItinerariesByUserId(@Param("userId") Long userId, Pageable pageable);

    Page<Itinerary> findByIsPublicTrue(Pageable pageable);
}
