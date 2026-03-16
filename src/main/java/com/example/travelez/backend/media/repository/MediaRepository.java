package com.example.travelez.backend.media.repository;

import com.example.travelez.backend.media.model.Media;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MediaRepository extends JpaRepository<Media, Long> {

    @Query("SELECT p.id, m FROM Posts p JOIN p.medias m WHERE p.id IN :postIds")
    List<Object[]> findAllByPostIds(@Param("postIds") List<Long> postIds);
}
