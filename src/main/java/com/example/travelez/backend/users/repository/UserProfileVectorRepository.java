package com.example.travelez.backend.users.repository;

import com.example.travelez.backend.users.model.UserProfileVector;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserProfileVectorRepository extends JpaRepository<UserProfileVector, Long> {
}