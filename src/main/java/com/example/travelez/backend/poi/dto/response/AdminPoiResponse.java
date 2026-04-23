package com.example.travelez.backend.poi.dto.response;

import com.example.travelez.backend.poi.model.enums.PoiStatus;
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
public class AdminPoiResponse extends PoiBaseResponse {
    private PlaceBaseResponse place;
    private WardBaseResponse ward;
}
