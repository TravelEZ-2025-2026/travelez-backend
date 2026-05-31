package com.example.travelez.backend.moderation.repository;

import com.example.travelez.backend.moderation.dto.internal.KeywordCacheEntry;
import com.example.travelez.backend.moderation.model.BannedKeyword;
import com.example.travelez.backend.moderation.model.enums.KeywordSeverity;
import com.example.travelez.backend.moderation.model.enums.ViolationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BannedKeywordRepository extends JpaRepository<BannedKeyword, Long>,
                                                  JpaSpecificationExecutor<BannedKeyword> {

    List<BannedKeyword> findAllByIsActiveTrue();

    @Query("SELECT new com.example.travelez.backend.moderation.dto.internal.KeywordCacheEntry(" +
           "k.id, k.keyword, k.violationType, k.severity, k.description) " +
           "FROM BannedKeyword k WHERE k.isActive = true")
    List<KeywordCacheEntry> findAllActiveAsEntry();

    Optional<BannedKeyword> findByKeyword(String keyword);

    boolean existsByKeyword(String keyword);

    List<BannedKeyword> findByViolationType(ViolationType violationType);

    List<BannedKeyword> findBySeverity(KeywordSeverity severity);

    long countByIsActiveTrue();
}
