package com.example.travelez.backend.poi.dto.response;

import java.util.List;

import com.example.travelez.backend.media.dto.response.MediaBaseResponse;
import com.example.travelez.backend.poi.model.enums.PlaceStatus;
import com.example.travelez.backend.poi.model.enums.PoiStatus;
import com.example.travelez.backend.poi.model.enums.PoiType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class PoiBaseResponse {
    private long id;
    private String name;
    private PoiType poiType;
    private String poiTypeDetail;
    private PoiStatus systemStatus;
    private String address;
    private String website;
    private Double latitude;
    private Double longitude;
    private Double rating;
    private Integer reviewCount;
    private PlaceStatus status;
    private String googleMapsUrl;
    private List<MediaBaseResponse> medias;
}
