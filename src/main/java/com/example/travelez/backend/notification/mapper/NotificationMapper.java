package com.example.travelez.backend.notification.mapper;

import com.example.travelez.backend.notification.dto.response.SystemNotificationResponse;
import com.example.travelez.backend.notification.enums.NotificationTargetType;
import com.example.travelez.backend.notification.model.Notification;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface NotificationMapper {
    
    @Mapping(target = "targetType", source = "targetType")
    @Mapping(target = "targetId", source = "targetId")
    SystemNotificationResponse toSystemNotificationResponse(Notification notification, NotificationTargetType targetType, Long targetId);
}
