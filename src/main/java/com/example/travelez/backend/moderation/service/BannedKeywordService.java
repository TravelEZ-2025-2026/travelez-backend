package com.example.travelez.backend.moderation.service;

import com.example.travelez.backend.common.api.CommonPage;
import com.example.travelez.backend.moderation.dto.request.BannedKeywordCreateRequest;
import com.example.travelez.backend.moderation.dto.request.BannedKeywordSearchRequest;
import com.example.travelez.backend.moderation.dto.request.BannedKeywordUpdateRequest;
import com.example.travelez.backend.moderation.dto.response.BannedKeywordResponse;
import org.springframework.data.domain.Pageable;

public interface BannedKeywordService {
    
    void createKeyword(BannedKeywordCreateRequest request);
    
    void updateKeyword(Long id, BannedKeywordUpdateRequest request);
    
    void deleteKeyword(Long id);
    
    BannedKeywordResponse getKeywordById(Long id);
    
    CommonPage<BannedKeywordResponse> searchKeywords(BannedKeywordSearchRequest request, Pageable pageable);
    
    void toggleKeyword(Long id);
    
    void refreshCache();
}
