package com.example.travelez.backend.posts.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class PostStatResponse {
    private Long totalPosts;
    private List<TopPoiResponse> topPois;
    private List<TopTagResponse> topTags;
    private Long totalComments;
}
