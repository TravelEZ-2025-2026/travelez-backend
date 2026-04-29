package com.example.travelez.backend.posts.event;

import com.example.travelez.backend.posts.model.Posts;
import com.example.travelez.backend.posts.model.enums.PostStatus;
import com.example.travelez.backend.posts.model.enums.PostStatusAction;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PostsStatusChangedEvent {
    private Posts posts;
    private Long adminId;
    private Long authorId;
    private PostStatus oldStatus;
    private PostStatus newStatus;
    private PostStatusAction action;
    private String reason;
}
