package com.example.travelez.backend.notification.dto.response;

import com.example.travelez.backend.notification.enums.NotificationTargetType;
import com.example.travelez.backend.notification.model.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class SystemNotificationResponse {
    private Long id;
    private String title;
    private String message;
    private NotificationType notificationType;
    private boolean isRead;
    private LocalDateTime createdAt;
    private NotificationTargetType targetType;
    private Long targetId;
}
