package com.example.travelez.backend.media.repository;

import com.example.travelez.backend.media.model.Media;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MediaRepository extends JpaRepository<Media, Long> {
}
