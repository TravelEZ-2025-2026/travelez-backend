package com.example.travelez.backend.itinerary.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class ItineraryCreationRequest {
    @NotEmpty(message = "Destination cities cannot be empty")
    private List<String> destinationCities; // VD: ["thanh_pho_ho_chi_minh"]

    @PositiveOrZero(message = "Budget must be a positive number")
    private BigDecimal budget = BigDecimal.valueOf(5_000_000);

    @NotNull(message = "Start date cannot be null")
    @FutureOrPresent(message = "Start date must be in the present or future")
    private LocalDate startDate;    // VD: 2025-11-11

    @NotNull(message = "End date cannot be null")
    private LocalDate endDate;      // VD: 2025-11-14

    @NotEmpty(message = "Choose at least one travel style")
    private List<String> styles;    // VD: ["Food Tourism", "Photography"]

    @NotBlank(message = "Companion information cannot be blank")
    private String companion;       // VD: Family Expedition

    @NotNull(message = "Must specify if you have kids or not")
    private Boolean hasKids;

    @NotNull(message = "Must specify if you have pets or not")
    private Boolean hasPets;

    private String specialNotes;    // VD: "Không ăn cay được"
}
