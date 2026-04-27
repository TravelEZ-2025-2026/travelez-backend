package com.example.travelez.backend.review.repository;

import com.example.travelez.backend.review.model.Review;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewRepository extends JpaRepository<Review, Long>, JpaSpecificationExecutor<Review> {

    @EntityGraph(attributePaths = {"poi", "traveler"})
    Page<Review> findAll(Specification<Review> specification, Pageable pageable);

    @Query("SELECT r.poi.id, m FROM Review r JOIN r.poi.medias m WHERE r.id IN :reviewIds")
    List<Object[]> findAllMediasByReviewIds(@Param("reviewIds") List<Long> reviewIds);

    @EntityGraph(attributePaths = {"poi", "traveler"})
    Page<Review> findAllByTravelerId(Long userId, Pageable pageable);
}
