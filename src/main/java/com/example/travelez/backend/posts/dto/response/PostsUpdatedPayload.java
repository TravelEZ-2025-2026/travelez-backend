package com.example.travelez.backend.posts.dto.response;

import com.example.travelez.backend.posts.model.enums.PostStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class PostsUpdatedPayload {
    private Long postId;
    private PostStatus status;
}
