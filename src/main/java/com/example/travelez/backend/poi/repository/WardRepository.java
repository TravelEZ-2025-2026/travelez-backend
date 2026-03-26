package com.example.travelez.backend.poi.repository;

import com.example.travelez.backend.poi.model.Ward;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface WardRepository extends JpaRepository<Ward, Long> {
    List<Ward> findAllByPlace_Id(Long placeId);
}
