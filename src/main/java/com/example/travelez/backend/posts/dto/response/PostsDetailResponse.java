package com.example.travelez.backend.posts.dto.response;

import com.example.travelez.backend.itinerary.dto.response.ItinerarySummaryResponse;
import com.example.travelez.backend.media.dto.response.MediaBaseResponse;
import com.example.travelez.backend.poi.dto.response.PoiSummaryResponse;
import com.example.travelez.backend.users.dto.response.UserSummaryResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class PostsDetailResponse {
    private Long id;
    private String title;
    private String content;
    private UserSummaryResponse author;
    private PoiSummaryResponse poiSummary;
    private ItinerarySummaryResponse itinerarySummary;
    private List<MediaBaseResponse> medias;
    private Long commentCount;
    private String createdAt;
    private String updatedAt;
}
