package com.daenggo.backend.place.dto;

import java.math.BigDecimal;

public record PlaceSearchCondition(
        BigDecimal swLat,
        BigDecimal swLng,
        BigDecimal neLat,
        BigDecimal neLng,
        String category,
        Boolean indoorAllowedOnly,    // 실내 동반 가능한 곳만
        BigDecimal petWeight,
        Boolean isDangerous
) {}