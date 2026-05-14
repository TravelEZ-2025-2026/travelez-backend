package com.example.travelez.backend.enhancement.repository;

import com.example.travelez.backend.enhancement.model.ItineraryEnhancementHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ItineraryEnhancementHistoryRepository extends JpaRepository<ItineraryEnhancementHistory, Long> {
    Page<ItineraryEnhancementHistory> findByProviderIdOrderByCreatedAtDesc(Long providerId, Pageable pageable);
    Optional<ItineraryEnhancementHistory> findByIdAndProviderId(Long id, Long providerId);
}
