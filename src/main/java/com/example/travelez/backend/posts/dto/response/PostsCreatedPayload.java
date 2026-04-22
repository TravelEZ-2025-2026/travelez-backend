package com.example.travelez.backend.posts.dto.response;

import com.example.travelez.backend.posts.model.enums.PostStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class PostsCreatedPayload {
    private Long postId;
    private String title;
    private String content;
    private PostStatus status;
    private LocalDateTime createdAt;
}
