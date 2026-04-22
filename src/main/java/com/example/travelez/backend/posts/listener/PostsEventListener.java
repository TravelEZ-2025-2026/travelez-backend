package com.example.travelez.backend.posts.listener;

import com.example.travelez.backend.posts.event.PostsCreatedEvent;
import com.example.travelez.backend.posts.event.PostsDeleteEvent;
import com.example.travelez.backend.posts.event.PostsUpdateEvent;
import com.example.travelez.backend.posts.service.PostsAiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PostsEventListener {

    private final PostsAiService postsAiService;

    @Async
    @EventListener
    public void handlePostCreated(PostsCreatedEvent event) {
        postsAiService.saveOrUpdatePostVector(event.getPayload());
    }

    @Async
    @EventListener
    public void handlePostDeleted(PostsDeleteEvent event) {
        postsAiService.deletePostVector(event.getPostId());
    }

    @Async
    @EventListener
    public void handlePostUpdated(PostsUpdateEvent event) {
        postsAiService.updatePostVector(event.getPayload());
    }
}
