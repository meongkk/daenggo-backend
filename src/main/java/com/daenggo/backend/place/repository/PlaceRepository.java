package com.daenggo.backend.place.repository;

import com.daenggo.backend.place.entity.Place;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface PlaceRepository extends JpaRepository<Place, Long> {

    Optional<Place> findByContentId(String contentId);

    boolean existsByContentId(String contentId);

    List<Place> findByLatitudeBetweenAndLongitudeBetween(
            BigDecimal minLat, BigDecimal maxLat,
            BigDecimal minLng, BigDecimal maxLng);

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
}