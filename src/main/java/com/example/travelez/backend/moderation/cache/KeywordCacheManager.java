package com.example.travelez.backend.moderation.cache;

import com.example.travelez.backend.moderation.dto.internal.KeywordCacheEntry;
import com.example.travelez.backend.moderation.repository.BannedKeywordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class KeywordCacheManager implements CommandLineRunner {

    private final BannedKeywordRepository bannedKeywordRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String CACHE_KEY = "moderation:keywords:all";
    private static final long CACHE_TTL_HOURS = 24;

    @Override
    public void run(String... args) {
        log.info("Warming up banned keywords cache...");
        try {
            warmUpCache();
            log.info("Banned keywords cache warmed up successfully");
        } catch (Exception e) {
            log.warn("Failed to warm up banned keywords cache on startup (table may not exist yet): {}", e.getMessage());
        }
    }

    public void warmUpCache() {
        List<KeywordCacheEntry> keywords = bannedKeywordRepository.findAllActiveAsEntry();
        redisTemplate.opsForValue().set(CACHE_KEY, keywords, CACHE_TTL_HOURS, TimeUnit.HOURS);
        log.info("Loaded {} active keywords to Redis cache", keywords.size());
    }

    @Cacheable(value = "keywords", key = "'all'")
    public List<KeywordCacheEntry> getActiveKeywords() {
        try {
            @SuppressWarnings("unchecked")
            List<KeywordCacheEntry> cached = (List<KeywordCacheEntry>) redisTemplate.opsForValue().get(CACHE_KEY);
            if (cached != null && !cached.isEmpty()) {
                log.debug("Retrieved {} keywords from Redis cache", cached.size());
                return cached;
            }
        } catch (Exception e) {
            log.warn("Redis cache failed, falling back to Caffeine: {}", e.getMessage());
        }

        List<KeywordCacheEntry> keywords = bannedKeywordRepository.findAllActiveAsEntry();
        log.debug("Retrieved {} keywords from database", keywords.size());
        return keywords;
    }

    @CacheEvict(value = "keywords", allEntries = true)
    public void refreshCache() {
        log.info("Refreshing banned keywords cache...");
        try {
            redisTemplate.delete(CACHE_KEY);
        } catch (Exception e) {
            log.warn("Failed to delete Redis cache: {}", e.getMessage());
        }
        warmUpCache();
        log.info("Cache refreshed successfully");
    }
}
