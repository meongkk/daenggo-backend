package com.daenggo.backend.place.repository;

import com.daenggo.backend.place.entity.PlaceCondition;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PlaceConditionRepository extends JpaRepository<PlaceCondition, Long> {
    Optional<PlaceCondition> findByPlace_PlaceId(Long placeId);
    boolean existsByPlace_PlaceId(Long placeId);
}