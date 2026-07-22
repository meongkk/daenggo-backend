package com.daenggo.backend.walk.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.daenggo.backend.walk.entity.WalkRecord;
import com.daenggo.backend.walk.entity.WalkRouteRecord;

public interface WalkRouteRecordRepository extends JpaRepository<WalkRouteRecord, Long> {

	// 경로 조회
    List<WalkRouteRecord> findByWalkOrderBySequenceNoAsc(WalkRecord walkRecord);
}
