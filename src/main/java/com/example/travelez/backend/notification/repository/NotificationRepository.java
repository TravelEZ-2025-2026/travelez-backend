package com.example.travelez.backend.notification.repository;

import com.example.travelez.backend.notification.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
}
