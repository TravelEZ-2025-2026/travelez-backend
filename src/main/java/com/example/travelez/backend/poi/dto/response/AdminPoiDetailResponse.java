package com.example.travelez.backend.poi.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AdminPoiDetailResponse extends PoiDetailResponse {
    private PlaceBaseResponse place;
    private WardBaseResponse ward;
}
