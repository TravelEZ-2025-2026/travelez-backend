package com.example.travelez.backend.notification.service;

import com.example.travelez.backend.notification.dto.request.SystemNotificationRequest;

public interface SystemNotificationService {
    void sendSystemNotification(SystemNotificationRequest request);
}
