package com.example.travelez.backend.itinerary.repository;

import com.example.travelez.backend.itinerary.model.ItinerarySharedUser;
import com.example.travelez.backend.itinerary.model.ItinerarySharedUserId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItinerarySharedUserRepository extends JpaRepository<ItinerarySharedUser, ItinerarySharedUserId> {
    List<ItinerarySharedUser> findByItineraryId(Long itineraryId);
    boolean existsByItineraryIdAndUserId(Long itineraryId, Long userId);

    @Query("SELECT s FROM ItinerarySharedUser s " +
            "JOIN FETCH s.user u " +
            "LEFT JOIN FETCH u.avatar " +
            "WHERE s.itineraryId = :itineraryId " +
            "AND LOWER(u.username) LIKE LOWER(CONCAT(:keyword, '%'))")
    List<ItinerarySharedUser> searchSharedUsersByKeyword(
            @Param("itineraryId") Long itineraryId,
            @Param("keyword") String keyword
    );

    @Query(value = "SELECT s FROM ItinerarySharedUser s " +
            "JOIN FETCH s.user u " +
            "LEFT JOIN FETCH u.avatar " +
            "WHERE s.itineraryId = :itineraryId",
            countQuery = "SELECT COUNT(s) FROM ItinerarySharedUser s WHERE s.itineraryId = :itineraryId")
    Page<ItinerarySharedUser> findByItineraryIdWithPage(
            @Param("itineraryId") Long itineraryId,
            Pageable pageable
    );
}
