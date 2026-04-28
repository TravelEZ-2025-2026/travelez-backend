package com.example.travelez.backend.itinerary.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ItinerarySharedUserId implements Serializable {
    private Long itineraryId;
    private Long userId;
}
