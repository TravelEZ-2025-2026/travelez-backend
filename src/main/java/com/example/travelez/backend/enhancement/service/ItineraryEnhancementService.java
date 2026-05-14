package com.example.travelez.backend.enhancement.service;

import com.example.travelez.backend.common.api.CommonPage;
import com.example.travelez.backend.enhancement.dto.response.HistoryDetailResponse;
import com.example.travelez.backend.enhancement.dto.response.ItineraryEnhancementHistorySummary;
import org.springframework.data.domain.Pageable;

public interface ItineraryEnhancementService {
    CommonPage<ItineraryEnhancementHistorySummary> getMyHistories(Pageable pageable);

    HistoryDetailResponse getHistoryDetail(Long historyId);

    void deleteHistory(Long historyId);
}
