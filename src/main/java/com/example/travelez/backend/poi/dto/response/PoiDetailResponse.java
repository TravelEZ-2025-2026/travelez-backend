package com.example.travelez.backend.poi.dto.response;

import java.util.List;
import java.util.Map;

import com.example.travelez.backend.media.dto.response.MediaBaseResponse;
import com.example.travelez.backend.poi.model.OpeningHours;
import com.example.travelez.backend.poi.model.ReviewDistributions;

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
public class PoiDetailResponse extends PoiBaseResponse {
    private String phoneNumber;
    private String description;
    private String googlePlaceId;
    private List<OpeningHours> openingHour;
    private ReviewDistributions reviewsDistribution;
    private Map<String, Boolean> additionalInfo;
    // private List<MediaBaseResponse> medias;
}
