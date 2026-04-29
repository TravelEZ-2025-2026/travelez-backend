package com.example.travelez.backend.posts.handler;

import com.example.travelez.backend.notification.dto.request.SystemNotificationRequest;
import com.example.travelez.backend.posts.event.PostsStatusChangedEvent;
import com.example.travelez.backend.posts.model.enums.PostStatusAction;

public interface PostsNotificationHandler {
    PostStatusAction getSupportedActionStatus();

    SystemNotificationRequest buildPayload(PostsStatusChangedEvent event);
}
