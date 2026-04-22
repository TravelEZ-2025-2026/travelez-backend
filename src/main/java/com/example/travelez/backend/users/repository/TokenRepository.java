package com.example.travelez.backend.users.repository;

import com.example.travelez.backend.users.model.UserOAthToken;
import com.example.travelez.backend.users.model.enums.AuthProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TokenRepository extends JpaRepository<UserOAthToken, Long> {
    Optional<UserOAthToken> findByUserIdAndProvider(Long userId, AuthProvider provider);
}
