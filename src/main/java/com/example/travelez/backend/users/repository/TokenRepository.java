package com.example.travelez.backend.users.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.travelez.backend.users.model.User;
import com.example.travelez.backend.users.model.UserOAthToken;

public interface TokenRepository extends JpaRepository<UserOAthToken, Long> {
    Optional<UserOAthToken> findByUserIdAndProvider(Long userId, User.AuthProvider provider);
}
