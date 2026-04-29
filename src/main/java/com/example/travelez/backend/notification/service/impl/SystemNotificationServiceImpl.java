package com.example.travelez.backend.notification.service.impl;

import com.example.travelez.backend.common.dto.SocketEvent;
import com.example.travelez.backend.common.model.enums.SocketEventType;
import com.example.travelez.backend.infrastructure.websocket.SocketService;
import com.example.travelez.backend.notification.dto.request.SystemNotificationRequest;
import com.example.travelez.backend.notification.dto.response.SystemNotificationResponse;
import com.example.travelez.backend.notification.mapper.NotificationMapper;
import com.example.travelez.backend.notification.model.Notification;
import com.example.travelez.backend.notification.repository.NotificationRepository;
import com.example.travelez.backend.notification.service.SystemNotificationService;
import com.example.travelez.backend.users.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SystemNotificationServiceImpl implements SystemNotificationService {

    private final SocketService socketService;

    private final NotificationRepository notificationRepository;

    private final NotificationMapper notificationMapper;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void sendSystemNotification(SystemNotificationRequest request) {
        Notification.NotificationBuilder builder = Notification.builder()
                .title(request.getTitle())
                .message(request.getMessage())
                .notificationType(request.getType())
                .user(User.builder().id(request.getRecipient().getId()).build());

        if (request.getEntityLinker() != null) {
            request.getEntityLinker().accept(builder);
        }
        Notification notification = notificationRepository.save(builder.build());

        SocketEvent<SystemNotificationResponse> event = SocketEvent.<SystemNotificationResponse>builder()
                .type(SocketEventType.SYSTEM_NOTIFICATION)
                .payload(notificationMapper.toSystemNotificationResponse(notification, request.getTargetType(), request.getTargetId()))
                .build();

        socketService.sendToTopic("/topic/user." + request.getRecipient().getId(), event);
    }
}
