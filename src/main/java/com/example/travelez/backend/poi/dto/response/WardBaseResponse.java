package com.example.travelez.backend.poi.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class WardBaseResponse {
    private long id;
    private String name;
    private String divisionType;
    private String description;
}
