package com.example.travelez.backend.posts.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class TopPoiResponse {
    private long id;
    private String name;
    private long countPost;
}
