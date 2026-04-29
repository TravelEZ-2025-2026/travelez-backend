package com.example.travelez.backend.ai.pipeline.model;

public interface PoiVectorResult {
    Long getId();
    String getName();
    String getPoiType();
    String getPoiTypeDetail();
    String getAddress();
    Double getLatitude();
    Double getLongitude();

    String getOpeningHour();

    String getGoogleMapsUrl();
    Double getRating();
    String getDescription();
    String getSemanticText();
}
