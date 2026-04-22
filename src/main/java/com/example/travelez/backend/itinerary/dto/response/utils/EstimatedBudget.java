package com.example.travelez.backend.itinerary.dto.response.utils;

import jakarta.persistence.Column;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class EstimatedBudget {
    private BigDecimal total ;
    private BigDecimal transportation;
    private BigDecimal activity;
    private BigDecimal foodAndDrink;
    private BigDecimal accommodation;
    private String currency;
}
