package com.example.travelez.backend.posts.event;

import com.example.travelez.backend.posts.dto.response.PostsUpdatedPayload;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PostsUpdateEvent {
    private PostsUpdatedPayload payload;
}
