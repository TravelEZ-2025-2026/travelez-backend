package com.example.travelez.backend.posts.listener;

import com.example.travelez.backend.notification.dto.request.SystemNotificationRequest;
import com.example.travelez.backend.notification.service.SystemNotificationService;
import com.example.travelez.backend.posts.event.PostsStatusChangedEvent;
import com.example.travelez.backend.posts.handler.PostsNotificationHandler;
import com.example.travelez.backend.posts.model.enums.PostStatusAction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class PostsNotificationListener {
    private final Map<PostStatusAction, PostsNotificationHandler> handlerMap;

    private final SystemNotificationService systemNotificationService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPostStatusChanged(PostsStatusChangedEvent event) {
        PostsNotificationHandler handler = handlerMap.get(event.getAction());

        if (handler == null) return;

        SystemNotificationRequest payload = handler.buildPayload(event);
        systemNotificationService.sendSystemNotification(payload);
    }

}
