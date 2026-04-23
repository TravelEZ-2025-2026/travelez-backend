package com.example.travelez.backend.itinerary.dto.request;

import com.example.travelez.backend.itinerary.dto.response.ItineraryResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class ItineraryReplanRequest extends ItineraryCreationRequest{
    private String feedbackNotes;
    private List<Long> rejectedPoiIds;
    private ItineraryResponse previousItinerary;
}
