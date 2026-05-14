package com.example.travelez.backend.enhancement.service.impl;

import com.example.travelez.backend.common.api.CommonPage;
import com.example.travelez.backend.common.api.ResultCode;
import com.example.travelez.backend.common.exception.ApiException;
import com.example.travelez.backend.common.utils.SecurityUtils;
import com.example.travelez.backend.enhancement.dto.response.HistoryDetailResponse;
import com.example.travelez.backend.enhancement.dto.response.ItineraryEnhancementHistorySummary;
import com.example.travelez.backend.enhancement.mapper.ItineraryEnhancementMapper;
import com.example.travelez.backend.enhancement.model.ItineraryEnhancementHistory;
import com.example.travelez.backend.enhancement.repository.ItineraryEnhancementHistoryRepository;
import com.example.travelez.backend.enhancement.service.ItineraryEnhancementService;
import com.example.travelez.backend.security.component.UserPrinciple;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ItineraryEnhancementServiceImpl implements ItineraryEnhancementService {
    private final ItineraryEnhancementHistoryRepository historyRepository;
    private final ItineraryEnhancementMapper mapper;

    @Override
    public CommonPage<ItineraryEnhancementHistorySummary> getMyHistories(Pageable pageable) {
        Long userId = SecurityUtils.getCurrentUserId();

        Page<ItineraryEnhancementHistory> historyPage = historyRepository.findByProviderIdOrderByCreatedAtDesc(userId, pageable);

        List<ItineraryEnhancementHistorySummary> summaries = historyPage.stream()
                .map(mapper::toSummary)
                .toList();

        return new CommonPage<>(
                summaries,
                historyPage.getTotalPages(),
                historyPage.getTotalElements(),
                pageable.getPageSize(),
                historyPage.getNumber(),
                historyPage.isEmpty()
        );
    }

    @Override
    public HistoryDetailResponse getHistoryDetail(Long historyId) {
        UserPrinciple currentUser = getCurrentUser();
        ItineraryEnhancementHistory history = historyRepository.findById(historyId)
                .orElseThrow(() -> new ApiException(ResultCode.NOT_FOUND, "History record not found."));

        if (!Objects.equals(history.getProvider().getId(), currentUser.getUserId())) {
            throw new ApiException(ResultCode.FORBIDDEN, "Access denied.");
        }

        return HistoryDetailResponse.builder()
                .id(history.getId())
                .fileName(history.getOriginalFileName())
                .providerPrompt(history.getProviderPrompt())
                .analysisResult(history.getAnalysisResult())
                .createdAt(history.getCreatedAt())
                .build();
    }

    @Override
    @Transactional
    public void deleteHistory(Long historyId) {
        Long userId = SecurityUtils.getCurrentUserId();
        ItineraryEnhancementHistory history = historyRepository.findByIdAndProviderId(historyId, userId)
                .orElseThrow(() -> new ApiException(ResultCode.NOT_FOUND, "History does not exist!"));
        historyRepository.delete(history);
    }

    private UserPrinciple getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new ApiException(ResultCode.UNAUTHORIZED, "You need to log in to perform this action.");
        }
        return (UserPrinciple) authentication.getPrincipal();
    }
}
