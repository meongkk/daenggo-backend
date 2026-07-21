package com.daenggo.backend.place.repository;

import com.daenggo.backend.place.entity.Place;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;
import java.math.BigDecimal;

public interface PlaceRepository extends JpaRepository<Place, Long> {

    // 캐싱 중복 확인용
    Optional<Place> findByContentId(String contentId);

    boolean existsByContentId(String contentId);

    // 지도 범위 내 장소 조회 (사각형 범위)
    List<Place> findByLatitudeBetweenAndLongitudeBetween(
            BigDecimal minLat, BigDecimal maxLat,
            BigDecimal minLng, BigDecimal maxLng);
}