package com.example.travelez.backend.ai.pipeline.model;

import com.example.travelez.backend.itinerary.dto.request.ItineraryCreationRequest;
import com.example.travelez.backend.itinerary.dto.response.ItineraryResponse;
import com.example.travelez.backend.poi.model.Poi;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PipelineContext {
    private ItineraryCreationRequest originalRequest;
    private SemanticQueryMap searchQueries;
    private List<PoiVectorResult> retrievedPois;
    private String rawLlmResponse;
    private ItineraryResponse finalResponse;
    private String userProfileVector;
}
