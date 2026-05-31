package com.example.travelez.backend.moderation.repository;

import com.example.travelez.backend.moderation.model.ModerationAlert;
import com.example.travelez.backend.moderation.model.enums.AlertStatus;
import com.example.travelez.backend.moderation.model.enums.ModerationTargetType;
import com.example.travelez.backend.moderation.model.enums.ViolationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface ModerationAlertRepository extends JpaRepository<ModerationAlert, Long>, 
                                                    JpaSpecificationExecutor<ModerationAlert> {
    
    Optional<ModerationAlert> findByTargetIdAndTargetType(Long targetId, ModerationTargetType targetType);
    
    List<ModerationAlert> findByStatus(AlertStatus status);
    
    List<ModerationAlert> findByTargetType(ModerationTargetType targetType);
    
    List<ModerationAlert> findByViolationType(ViolationType violationType);
    
    long countByStatus(AlertStatus status);
    
    long countByViolationType(ViolationType violationType);
    
    long countByTargetType(ModerationTargetType targetType);
    
    @Query("SELECT COUNT(a) FROM ModerationAlert a WHERE a.status = 'PENDING'")
    long countPendingAlerts();
    
    @Query("SELECT a.violationType, COUNT(a) FROM ModerationAlert a GROUP BY a.violationType")
    List<Object[]> countByViolationTypeGrouped();
    
    @Query("SELECT a.targetType, COUNT(a) FROM ModerationAlert a GROUP BY a.targetType")
    List<Object[]> countByTargetTypeGrouped();
}
