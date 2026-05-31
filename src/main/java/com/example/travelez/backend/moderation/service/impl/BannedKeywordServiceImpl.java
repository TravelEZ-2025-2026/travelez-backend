package com.example.travelez.backend.moderation.service.impl;

import com.example.travelez.backend.common.api.CommonPage;
import com.example.travelez.backend.common.api.ResultCode;
import com.example.travelez.backend.common.exception.ApiException;
import com.example.travelez.backend.moderation.cache.KeywordCacheManager;
import com.example.travelez.backend.moderation.dto.request.BannedKeywordCreateRequest;
import com.example.travelez.backend.moderation.dto.request.BannedKeywordSearchRequest;
import com.example.travelez.backend.moderation.dto.request.BannedKeywordUpdateRequest;
import com.example.travelez.backend.moderation.dto.response.BannedKeywordResponse;
import com.example.travelez.backend.moderation.mapper.BannedKeywordMapper;
import com.example.travelez.backend.moderation.model.BannedKeyword;
import com.example.travelez.backend.moderation.repository.BannedKeywordRepository;
import com.example.travelez.backend.moderation.service.BannedKeywordService;
import com.example.travelez.backend.moderation.repository.specification.BannedKeywordSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BannedKeywordServiceImpl implements BannedKeywordService {

    private final BannedKeywordRepository repository;
    private final BannedKeywordMapper mapper;
    private final KeywordCacheManager cacheManager;

    @Override
    @Transactional
    public void createKeyword(BannedKeywordCreateRequest request) {
        if (repository.existsByKeyword(request.getKeyword())) {
            throw new ApiException(ResultCode.BAD_REQUEST, "Keyword already exists");
        }
        
        BannedKeyword keyword = mapper.toEntity(request);
        keyword.setIsActive(true);
        repository.save(keyword);
        
        cacheManager.refreshCache();
        log.info("Created banned keyword: {}", request.getKeyword());
    }

    @Override
    @Transactional
    public void updateKeyword(Long id, BannedKeywordUpdateRequest request) {
        BannedKeyword keyword = repository.findById(id)
                .orElseThrow(() -> new ApiException(ResultCode.NOT_FOUND, "Keyword not found"));
        
        mapper.updateEntityFromRequest(request, keyword);
        repository.save(keyword);
        
        cacheManager.refreshCache();
        log.info("Updated banned keyword: {}", id);
    }

    @Override
    @Transactional
    public void deleteKeyword(Long id) {
        if (!repository.existsById(id)) {
            throw new ApiException(ResultCode.NOT_FOUND, "Keyword not found");
        }
        
        repository.deleteById(id);
        cacheManager.refreshCache();
        log.info("Deleted banned keyword: {}", id);
    }

    @Override
    public BannedKeywordResponse getKeywordById(Long id) {
        BannedKeyword keyword = repository.findById(id)
                .orElseThrow(() -> new ApiException(ResultCode.NOT_FOUND, "Keyword not found"));
        return mapper.toResponse(keyword);
    }

    @Override
    public CommonPage<BannedKeywordResponse> searchKeywords(BannedKeywordSearchRequest request, Pageable pageable) {
        List<Specification<BannedKeyword>> specs = new ArrayList<>();
        specs.add(BannedKeywordSpecification.filterByViolationType(request.getViolationType()));
        specs.add(BannedKeywordSpecification.filterBySeverity(request.getSeverity()));
        specs.add(BannedKeywordSpecification.filterByIsActive(request.getIsActive()));
        specs.add(BannedKeywordSpecification.filterByKeyword(request.getKeyword()));

        Page<BannedKeyword> page = repository.findAll(Specification.allOf(specs), pageable);
        List<BannedKeywordResponse> responses = page.getContent().stream()
                .map(mapper::toResponse)
                .toList();
        return new CommonPage<>(responses, page.getTotalPages(), page.getTotalElements(), 
                page.getSize(), page.getNumber(), page.isEmpty());
    }

    @Override
    @Transactional
    public void toggleKeyword(Long id) {
        BannedKeyword keyword = repository.findById(id)
                .orElseThrow(() -> new ApiException(ResultCode.NOT_FOUND, "Keyword not found"));
        
        keyword.setIsActive(!keyword.getIsActive());
        repository.save(keyword);
        
        cacheManager.refreshCache();
        log.info("Toggled keyword {} to {}", id, keyword.getIsActive());
    }

    @Override
    public void refreshCache() {
        cacheManager.refreshCache();
        log.info("Cache refreshed manually");
    }

}
