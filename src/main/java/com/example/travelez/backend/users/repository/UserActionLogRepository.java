package com.example.travelez.backend.users.repository;

import com.example.travelez.backend.users.model.UserActionLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserActionLogRepository extends JpaRepository<UserActionLog, Long> {
}
