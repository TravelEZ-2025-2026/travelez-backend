package com.example.travelez.backend.users.repository;

import com.example.travelez.backend.users.model.UserProfileVector;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserProfileVectorRepository extends JpaRepository<UserProfileVector, Long> {

    @Modifying
    @Query(value = "INSERT INTO user_profile_vector (user_id, profile_vector) " +
            "VALUES (:userId, cast(:vectorStr as vector)) " +
            "ON CONFLICT (user_id) DO UPDATE " +
            "SET profile_vector = EXCLUDED.profile_vector, updated_at = NOW()",
            nativeQuery = true)
    void upsertProfileVector(@Param("userId") Long userId, @Param("vectorStr") String vectorStr);
}