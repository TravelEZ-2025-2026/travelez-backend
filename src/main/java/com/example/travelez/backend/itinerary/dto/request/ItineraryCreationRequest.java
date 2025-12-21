package com.example.travelez.backend.itinerary.dto.request;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class ItineraryCreationRequest {
    private List<String> destinationCities; // VD: ["thanh_pho_ho_chi_minh"]
    private BigDecimal budget;
    private LocalDate startDate;    // VD: 2025-11-11
    private LocalDate endDate;      // VD: 2025-11-14
    private List<String> styles;    // VD: ["Food Tourism", "Photography"]
    private String companion;       // VD: Family Expedition

    private Boolean hasKids;
    private Boolean hasPets;

    private String specialNotes;    // VD: "Không ăn cay được"
}
