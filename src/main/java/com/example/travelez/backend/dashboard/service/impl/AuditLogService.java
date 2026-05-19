package com.example.travelez.backend.dashboard.service.impl;

import com.example.travelez.backend.dashboard.model.SystemActivityLog;
import com.example.travelez.backend.dashboard.model.enums.ActivityCategory;
import com.example.travelez.backend.dashboard.repository.SystemActivityLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuditLogService {
    private final SystemActivityLogRepository logRepository;

    public void logActivity(ActivityCategory category, String description, String status) {
        SystemActivityLog log = SystemActivityLog.builder()
                .category(category)
                .description(description)
                .status(status)
                .createdAt(LocalDateTime.now())
                .build();
        logRepository.save(log);
    }
}
