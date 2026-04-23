package com.example.travelez.backend.itinerary.repository;

import com.example.travelez.backend.itinerary.model.ItinerarySharedUser;
import com.example.travelez.backend.itinerary.model.ItinerarySharedUserId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItinerarySharedUserRepository extends JpaRepository<ItinerarySharedUser, ItinerarySharedUserId> {
    List<ItinerarySharedUser> findByItineraryId(Long itineraryId);
    boolean existsByItineraryIdAndUserId(Long itineraryId, Long userId);
}
