package com.example.travelez.backend.posts.dto.response;

import com.example.travelez.backend.itinerary.dto.response.ItinerarySummaryResponse;
import com.example.travelez.backend.media.dto.response.MediaBaseResponse;
import com.example.travelez.backend.poi.dto.response.PoiSummaryResponse;
import com.example.travelez.backend.posts.model.enums.PostStatus;
import com.example.travelez.backend.users.dto.response.UserSummaryResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.List;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class PostResponse {
    private Long id;
    private String title;
    private String topicTag;
    private String content;
    private PoiSummaryResponse poiSummary;
    private ItinerarySummaryResponse itinerarySummary;
    private PostStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private UserSummaryResponse author;

    private Long commentCount;

    private Long reactionCount;
    private boolean isReactedByMe;

    private List<MediaBaseResponse> medias;

    private boolean isFollowed;
}
