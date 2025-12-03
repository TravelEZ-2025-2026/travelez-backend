package com.example.travelez.backend.itinerary.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
// Dòng này cực quan trọng: Nếu JSON có trường lạ chưa khai báo trong Java, nó sẽ bỏ qua thay vì báo lỗi 500
@JsonIgnoreProperties(ignoreUnknown = true)
public class PoiData {
    private String id;
    private String name;
    private String type;
    private double rating;
    private String description;
    private String address;

    // --- FIX LỖI TẠI ĐÂY ---

    // Ánh xạ "opening_hours" (JSON) -> openingHours (Java)
    @JsonProperty("opening_hours")
    private String openingHours;

    // Bổ sung thêm các trường có trong JSON mock để tận dụng hết dữ liệu
    @JsonProperty("price_level")
    private String priceLevel;

    @JsonProperty("best_time_to_visit")
    private String bestTimeToVisit;
}