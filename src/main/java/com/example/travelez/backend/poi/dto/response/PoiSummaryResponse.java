package com.example.travelez.backend.poi.dto.response;

import com.example.travelez.backend.poi.model.enums.PoiType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class PoiSummaryResponse {
    private long id;
    private String name;
    private PoiType poiType;
    private String poiTypeDetail;
}
