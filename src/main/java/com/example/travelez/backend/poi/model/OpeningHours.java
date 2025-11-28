package com.example.travelez.backend.poi.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OpeningHours implements Serializable {
    private String day;
    private String hours;
}
