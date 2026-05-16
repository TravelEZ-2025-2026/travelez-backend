package com.example.travelez.backend.users.listener;

import com.example.travelez.backend.users.event.UserStatusChangedEvent;
import com.example.travelez.backend.users.model.UserActionLog;
import com.example.travelez.backend.users.repository.UserActionLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserStatusChangedListener {
    
    private final UserActionLogRepository userActionLogRepository;
    
    @EventListener
    public void onUserStatusChanged(UserStatusChangedEvent event) {
        log.info("User status changed: userId={}, action={}, reason={}", 
                event.getUserId(), event.getActionType(), event.getReason());
        
        // Ghi log vào database
        UserActionLog actionLog = UserActionLog.builder()
                .userId(event.getUserId())
                .actionType(event.getActionType())
                .reason(event.getReason())
                .build();
        
        userActionLogRepository.save(actionLog);
        
        // TODO: Gửi notification cho user
        // notificationService.sendUserStatusNotification(event);
    }
}
