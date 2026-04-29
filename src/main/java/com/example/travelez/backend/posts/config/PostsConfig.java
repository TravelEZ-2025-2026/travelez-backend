package com.example.travelez.backend.posts.config;

import com.example.travelez.backend.posts.handler.PostsNotificationHandler;
import com.example.travelez.backend.posts.model.enums.PostStatusAction;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Configuration
public class PostsConfig {
    @Bean
    public Map<PostStatusAction, PostsNotificationHandler> postsNotificationHandlerMap(List<PostsNotificationHandler> handlers) {
        return handlers.stream()
                .collect(Collectors.toMap(PostsNotificationHandler::getSupportedActionStatus, Function.identity()));
    }
}
