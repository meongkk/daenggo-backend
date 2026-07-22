package com.daenggo.backend.place.repository;

import com.daenggo.backend.place.entity.Place;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface PlaceRepository extends JpaRepository<Place, Long> {

	/** 관광공사 콘텐츠 ID로 장소 조회 (데이터 갱신 시 기존 장소 확인용) */
    Optional<Place> findByContentId(String contentId);

    /** 관광공사 콘텐츠 ID 존재 여부 (동기화 시 중복 저장 방지용) */
    boolean existsByContentId(String contentId);

    /**
     * 지도 영역 + 반려동물 조건으로 장소 검색
     *
     * 파라미터가 null 또는 false면 해당 조건은 무시된다.
     * 출입이 명시적으로 불가한 장소(DENIED)는 조건과 무관하게 항상 제외한다.
     */
    @Query("""
        SELECT p FROM Place p
        LEFT JOIN PlaceCondition c ON c.place = p
        WHERE p.latitude BETWEEN :swLat AND :neLat
          AND p.longitude BETWEEN :swLng AND :neLng
          AND (:category IS NULL OR p.category = :category)
          AND (:indoorAllowedOnly = false OR c.indoorStatus = 'ALLOWED')
          AND (:petWeight IS NULL OR c.maxWeight IS NULL OR c.maxWeight >= :petWeight)
          AND (:isDangerous = false OR c.dangerousAllowed <> 'DENIED')
          AND (c.indoorStatus IS NULL OR c.indoorStatus <> 'DENIED')
        """)
    List<Place> searchByCondition(
            @Param("swLat") BigDecimal swLat,
            @Param("neLat") BigDecimal neLat,
            @Param("swLng") BigDecimal swLng,
            @Param("neLng") BigDecimal neLng,
            @Param("category") String category,
            @Param("indoorAllowedOnly") boolean indoorAllowedOnly,
            @Param("petWeight") BigDecimal petWeight,
            @Param("isDangerous") boolean isDangerous);
    
    /** 키워드로 장소명 또는 주소 검색 (페이징) */
    @Query("""
    	    SELECT p FROM Place p
    	    WHERE p.title LIKE %:keyword%
    	       OR p.address LIKE %:keyword%
    	    ORDER BY
    	        CASE WHEN p.title LIKE %:keyword% THEN 0 ELSE 1 END,
    	        p.title
    	    """)
    	Page<Place> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
}