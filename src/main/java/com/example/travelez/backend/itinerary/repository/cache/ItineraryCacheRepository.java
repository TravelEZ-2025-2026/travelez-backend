package com.example.travelez.backend.itinerary.repository.cache;

import com.example.travelez.backend.itinerary.dto.response.ItineraryResponse;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class ItineraryCacheRepository {
    private final Map<String, ItineraryResponse> cache = new ConcurrentHashMap<>();

    public void save(String id, ItineraryResponse data) {
        cache.put(id, data);
    }

    public ItineraryResponse get(String id) {
        return cache.get(id);
    }
}
