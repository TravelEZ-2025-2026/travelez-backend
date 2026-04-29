package com.example.travelez.backend.posts.repository;

import com.example.travelez.backend.posts.model.PostStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostStatusHistoryRepository extends JpaRepository<PostStatusHistory, Long> {
}
