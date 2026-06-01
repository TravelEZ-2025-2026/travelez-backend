package com.example.travelez.backend.itinerary.listener;

import com.example.travelez.backend.infrastructure.gemini.GeminiEmbeddingService;
import com.example.travelez.backend.itinerary.event.ItinerarySavedEvent;
import com.example.travelez.backend.itinerary.repository.ItineraryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ItineraryEventListener {
    private final GeminiEmbeddingService geminiEmbeddingService;
    private final ItineraryRepository itineraryRepository;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleItinerarySaved(ItinerarySavedEvent event) {
        try {
            List<float[]> embeddings = geminiEmbeddingService.embedTexts(List.of(event.objectives()));

            if (!embeddings.isEmpty() && embeddings.getFirst() != null) {
                String vectorStr = Arrays.toString(embeddings.getFirst());
                itineraryRepository.updateObjectivesVector(event.itineraryId(), vectorStr);
            }
        } catch (Exception e) {
            log.error("Error creating vector for Itinerary ID: {}", event.itineraryId(), e);
        }
    }
}
