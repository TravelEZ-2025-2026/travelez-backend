package com.example.travelez.backend.itinerary.dto.response;

import lombok.Data;

@Data
public class ActivityDTO {
    private long id;
    private String title;
    private String startTime;
    private String endTime;
    private String price;

    private String activityName;
    private String activityType;
    private String address;
    private String aiTip;
    private String image;
    private Double lat;
    private Double lng;
}
