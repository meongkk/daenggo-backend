package com.daenggo.backend.place.repository;

import com.daenggo.backend.place.entity.PolicyHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PolicyHistoryRepository extends JpaRepository<PolicyHistory, Long> {

    /** 특정 장소의 규정 변경 이력 (최신순) */
    List<PolicyHistory> findByPlace_PlaceIdOrderByCreatedAtDesc(Long placeId);
}