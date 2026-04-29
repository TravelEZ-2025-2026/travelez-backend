package com.example.travelez.backend.posts.handler;

import com.example.travelez.backend.notification.dto.request.SystemNotificationRequest;
import com.example.travelez.backend.notification.enums.NotificationTargetType;
import com.example.travelez.backend.notification.model.enums.NotificationType;
import com.example.travelez.backend.posts.event.PostsStatusChangedEvent;
import com.example.travelez.backend.posts.model.Posts;
import com.example.travelez.backend.posts.model.enums.PostStatusAction;
import com.example.travelez.backend.users.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class BannedPostHandler implements PostsNotificationHandler {

    @Override
    public PostStatusAction getSupportedActionStatus() {
        return PostStatusAction.BANNED;
    }

    @Override
    public SystemNotificationRequest buildPayload(PostsStatusChangedEvent event) {
        return SystemNotificationRequest.builder()
                .recipient(User.builder().id(event.getAuthorId()).build())
                .title("Post banned")
                .message("Your post has been banned. Reason: " + event.getReason())
                .type(NotificationType.POST_BANNED)
                .entityLinker(builder -> {
                    Posts posts = new Posts();
                    posts.setId(event.getPosts().getId());
                    builder.posts(posts);
                })
                .targetType(NotificationTargetType.POSTS)
                .targetId(event.getPosts().getId())
                .build();
    }
}
