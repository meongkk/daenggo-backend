package com.daenggo.backend.walk.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.daenggo.backend.user.entity.User;
import com.daenggo.backend.walk.entity.WalkRecord;

public interface WalkRecordRepository extends JpaRepository<WalkRecord, Long> {
		
//	// 회원의 전체 산책 목록
//    List<WalkRecord> findByUserOrderByStartedAtDesc(User user);

    // 회원의 특정 산책 조회
    Optional<WalkRecord> findByWalkRecordIdAndUser(Long walkRecordId, User user);

    // 월별 산책 조회 (캘린더)
    List<WalkRecord> findByUserAndStartedAtBetween(
            User user,
            LocalDateTime start,
            LocalDateTime end
    );
}
