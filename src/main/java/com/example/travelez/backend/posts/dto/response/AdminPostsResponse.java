package com.example.travelez.backend.posts.dto.response;

import com.example.travelez.backend.poi.dto.response.PoiSummaryResponse;
import com.example.travelez.backend.posts.model.enums.PostStatus;
import com.example.travelez.backend.users.dto.response.UserSummaryResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class AdminPostsResponse {
    private Long id;
    private String title;
    private String topicTag;
    private String content;
    private PoiSummaryResponse poiSummary;
    private PostStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private UserSummaryResponse author;

    private Long commentCount;

    private Long reactionCount;
}
