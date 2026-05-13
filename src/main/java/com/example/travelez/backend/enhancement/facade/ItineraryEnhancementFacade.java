package com.example.travelez.backend.enhancement.facade;

import com.example.travelez.backend.common.api.ResultCode;
import com.example.travelez.backend.common.exception.ApiException;
import com.example.travelez.backend.enhancement.dto.response.ItineraryEnhancementResponse;
import com.example.travelez.backend.enhancement.mapper.ItineraryEnhancementMapper;
import com.example.travelez.backend.enhancement.model.ItineraryEnhancementContext;
import com.example.travelez.backend.enhancement.model.ItineraryEnhancementHistory;
import com.example.travelez.backend.enhancement.pipeline.Phase1Extraction;
import com.example.travelez.backend.enhancement.pipeline.Phase2Matching;
import com.example.travelez.backend.enhancement.pipeline.Phase3InsightPool;
import com.example.travelez.backend.enhancement.pipeline.Phase4Analysis;
import com.example.travelez.backend.enhancement.repository.ItineraryEnhancementHistoryRepository;
import com.example.travelez.backend.security.component.UserPrinciple;
import com.example.travelez.backend.users.model.User;
import com.example.travelez.backend.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ItineraryEnhancementFacade {
    private final Phase1Extraction phase1Extraction;
    private final Phase2Matching phase2Matching;
    private final Phase3InsightPool phase3InsightPool;
    private final Phase4Analysis phase4Analysis;

    private final UserRepository userRepository;
    private final ItineraryEnhancementHistoryRepository historyRepository;
    private final ItineraryEnhancementMapper mapper;

    @Transactional
    public Long analyzeAndEnhance(MultipartFile documentFile, String providerPrompt) {
        UserPrinciple currentUser = getCurrentUser();

        User provider = userRepository.findById(currentUser.getUserId())
                .orElseThrow(() -> new ApiException(ResultCode.NOT_FOUND, "User information not found" ));

        Long currentUserId = currentUser.getUserId();

        if (currentUserId == null) {
            throw new ApiException(ResultCode.UNAUTHORIZED, "Login required to use AI Enhancement.");
        }

        ItineraryEnhancementContext context = new ItineraryEnhancementContext();
        context.setDocumentFile(documentFile);
        context.setProviderPrompt(providerPrompt);

        phase1Extraction.execute(context);
        phase2Matching.execute(context);
        phase3InsightPool.execute(context);
        ItineraryEnhancementResponse response = phase4Analysis.execute(context);

        ItineraryEnhancementHistory record = mapper.toEntity(
                provider,
                documentFile.getOriginalFilename(),
                providerPrompt,
                response
        );

        record = historyRepository.save(record);

        return record.getId();
    }

    private UserPrinciple getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new ApiException(ResultCode.UNAUTHORIZED, "You need to log in to perform this action.");
        }
        return (UserPrinciple) authentication.getPrincipal();
    }
}
