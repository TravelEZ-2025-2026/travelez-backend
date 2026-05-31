package com.example.travelez.backend.moderation.filter;

import com.example.travelez.backend.moderation.cache.KeywordCacheManager;
import com.example.travelez.backend.moderation.dto.internal.ContentCheckResult;
import com.example.travelez.backend.moderation.dto.internal.KeywordCacheEntry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ahocorasick.trie.Emit;
import org.ahocorasick.trie.Trie;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class KeywordFilter {

    private final KeywordCacheManager keywordCacheManager;

    public ContentCheckResult checkContent(String content) {
        if (content == null || content.isBlank()) {
            return ContentCheckResult.builder()
                    .isSafe(true)
                    .build();
        }

        List<KeywordCacheEntry> keywords = keywordCacheManager.getActiveKeywords();
        if (keywords.isEmpty()) {
            return ContentCheckResult.builder()
                    .isSafe(true)
                    .build();
        }

        Trie trie = buildTrie(keywords);
        String normalizedContent = normalizeContent(content);

        Collection<Emit> emits = trie.parseText(normalizedContent);

        if (emits.isEmpty()) {
            return ContentCheckResult.builder()
                    .isSafe(true)
                    .build();
        }

        Emit firstEmit = emits.iterator().next();
        KeywordCacheEntry matchedKeyword = keywords.stream()
                .filter(k -> k.keyword().equalsIgnoreCase(firstEmit.getKeyword()))
                .findFirst()
                .orElse(null);

        if (matchedKeyword == null) {
            return ContentCheckResult.builder()
                    .isSafe(true)
                    .build();
        }

        log.warn("Content contains banned keyword: '{}' (type: {}, severity: {})",
                matchedKeyword.keyword(),
                matchedKeyword.violationType(),
                matchedKeyword.severity());

        return ContentCheckResult.builder()
                .isSafe(false)
                .violationType(matchedKeyword.violationType())
                .matchedKeyword(matchedKeyword.keyword())
                .severity(matchedKeyword.severity())
                .reason(String.format("Nội dung chứa từ khóa vi phạm: '%s'", matchedKeyword.keyword()))
                .build();
    }

    private Trie buildTrie(List<KeywordCacheEntry> keywords) {
        Trie.TrieBuilder builder = Trie.builder()
                .ignoreCase()
                .ignoreOverlaps();

        keywords.forEach(keyword -> builder.addKeyword(keyword.keyword()));

        return builder.build();
    }

    private String normalizeContent(String content) {
        return content.trim().replaceAll("\\s+", " ");
    }
}
