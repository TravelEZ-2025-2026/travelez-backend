package com.example.travelez.backend.itinerary.dto.request;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class CreateItineraryRequest {
    private String destinationCity; // VD: thanh_pho_ho_chi_minh
    private String budgetLevel;     // VD: 5 Million VND
    private LocalDate startDate;    // VD: 2025-11-11
    private LocalDate endDate;      // VD: 2025-11-14
    private List<String> styles;    // VD: ["Food Tourism", "Photography"]
    private String companion;       // VD: Family Expedition

    private Boolean hasKids;
    private Boolean hasPets;

    private String specialNotes;    // VD: "Không ăn cay được"
}
