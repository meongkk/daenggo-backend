package com.daenggo.backend.place.repository;

import com.daenggo.backend.place.entity.PlaceReport;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PlaceReportRepository extends JpaRepository<PlaceReport, Long> {

    // 내가 작성한 신고 목록
    List<PlaceReport> findByUser_IdOrderByCreatedAtDesc(Long userId);

    // 특정 장소의 신고 목록
    List<PlaceReport> findByPlace_PlaceId(Long placeId);
}