package com.daenggo.backend.place.dto;

import java.math.BigDecimal;

/**
 * 장소 검색 조건
 *
 * swLat/swLng는 지도 화면의 남서쪽, neLat/neLng는 북동쪽 좌표.
 * 나머지 조건은 값이 없으면 필터링하지 않음.
 */
public record PlaceSearchCondition(
        BigDecimal swLat,
        BigDecimal swLng,
        BigDecimal neLat,
        BigDecimal neLng,
        String category,
        Boolean indoorAllowedOnly,
        BigDecimal petWeight,
        Boolean isDangerous
) {}