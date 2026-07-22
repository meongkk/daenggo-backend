package com.daenggo.backend.place.dto;

import com.daenggo.backend.place.entity.Place;
import java.math.BigDecimal;

public record PlaceNearbyResponse(
        Long placeId,
        String title,
        String category,
        BigDecimal latitude,
        BigDecimal longitude,
        String thumbnail
) {
    public static PlaceNearbyResponse from(Place place) {
        return new PlaceNearbyResponse(
                place.getPlaceId(),
                place.getTitle(),
                place.getCategory(),
                place.getLatitude(),
                place.getLongitude(),
                place.getThumbnail()
        );
    }
}