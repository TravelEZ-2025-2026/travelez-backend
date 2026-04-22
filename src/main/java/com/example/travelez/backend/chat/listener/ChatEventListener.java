package com.example.travelez.backend.chat.listener;

import com.example.travelez.backend.chat.event.MessageRecalledEvent;
import com.example.travelez.backend.chat.event.MessageSaveEvent;
import com.example.travelez.backend.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChatEventListener {
    private final NotificationService notificationService;

    //    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    @EventListener
    public void handleMessageSaved(MessageSaveEvent event) {
        notificationService.handleNewMessageNotification(event.getMessageResponse(), event.getConversationSocketResponse());
    }

    @Async
    @EventListener
    public void handleMessageRecalled(MessageRecalledEvent event) {
        notificationService.handleMessageRecalledNotification(event.getPayload());
    }
}
