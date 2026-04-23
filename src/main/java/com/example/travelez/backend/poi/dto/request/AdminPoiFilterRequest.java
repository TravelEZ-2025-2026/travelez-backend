package com.example.travelez.backend.poi.dto.request;

import com.example.travelez.backend.poi.model.enums.PoiStatus;
import com.example.travelez.backend.poi.model.enums.PoiType;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminPoiFilterRequest {
    private Long placeId;
    private Long wardId;
    private String name;
    private PoiType poiType;
    private PoiStatus poiStatus;
    private Double rating;
}
