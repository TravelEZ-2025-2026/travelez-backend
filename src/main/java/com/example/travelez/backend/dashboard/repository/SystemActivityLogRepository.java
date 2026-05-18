package com.example.travelez.backend.dashboard.repository;

import com.example.travelez.backend.dashboard.model.SystemActivityLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SystemActivityLogRepository extends JpaRepository<SystemActivityLog, Long> {

    @Query("SELECT l FROM SystemActivityLog l WHERE :filter = 'ALL' OR CAST(l.category AS string) = :filter")
    Page<SystemActivityLog> findRecentActivities(@Param("filter") String filter, Pageable pageable);
}
