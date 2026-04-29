package com.example.travelez.backend.notification.dto.request;

import com.example.travelez.backend.notification.enums.NotificationTargetType;
import com.example.travelez.backend.notification.model.Notification;
import com.example.travelez.backend.notification.model.enums.NotificationType;
import com.example.travelez.backend.users.model.User;
import lombok.Builder;
import lombok.Data;

import java.util.function.Consumer;

@Data
@Builder
public class SystemNotificationRequest {
    private User recipient;
    private String title;
    private String message;
    private NotificationType type;

    private Consumer<Notification.NotificationBuilder> entityLinker;

    private NotificationTargetType targetType;
    private Long targetId;
}
