package com.example.travelez.backend.enhancement.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItineraryEnhancementResponse implements Serializable {
    private List<String> itineraryPros;
    private List<String> experienceGaps;
    private List<Enhancement> actionableEnhancements;
    private List<Addition> suggestedAdditions;
    private List<Advisory> externalPoiAdvisories;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Enhancement implements Serializable {
        private String poiName;
        private String insightType;
        private String evidence;
        private String advice;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Addition implements Serializable {
        private String proposedPoi;
        private String targetGap;
        private String evidence;
        private String reason;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Advisory implements Serializable {
        private String poiName;
        private String advisory;
    }
}
