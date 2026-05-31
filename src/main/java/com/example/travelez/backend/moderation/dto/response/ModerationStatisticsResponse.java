package com.example.travelez.backend.moderation.dto.response;

import com.example.travelez.backend.moderation.model.enums.ViolationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModerationStatisticsResponse {
    private LocalDateTime fromDate;
    private LocalDateTime toDate;
    private List<DailyModerationStats> dailyStats;
    private Map<ViolationType, Long> violationStats;
    private List<KeywordViolationStats> topViolatedKeywords;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class DailyModerationStats {
    private LocalDate date;
    private Long totalAlerts;
    private Long approved;
    private Long banned;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class KeywordViolationStats {
    private String keyword;
    private Long count;
    private ViolationType violationType;
}
