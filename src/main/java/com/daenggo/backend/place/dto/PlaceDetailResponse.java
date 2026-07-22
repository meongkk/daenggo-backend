package com.daenggo.backend.place.dto;

import com.daenggo.backend.place.entity.Place;
import com.daenggo.backend.place.entity.PlaceCondition;
import java.math.BigDecimal;

public record PlaceDetailResponse(
        Long placeId,
        String title,
        String category,
        String address,
        String tel,
        BigDecimal latitude,
        BigDecimal longitude,
        String thumbnail,
        ConditionResponse condition
) {

    public record ConditionResponse(
            String indoorStatus,
            String leashRequired,
            String muzzleRequired,
            String dangerousAllowed,
            String allowedSize,
            BigDecimal maxWeight,
            String amenities,
            String rawText
    ) {
        public static ConditionResponse from(PlaceCondition c) {
            if (c == null) return null;
            return new ConditionResponse(
                    c.getIndoorStatus(),
                    c.getLeashRequired(),
                    c.getMuzzleRequired(),
                    c.getDangerousAllowed(),
                    c.getAllowedSize(),
                    c.getMaxWeight(),
                    c.getAmenities(),
                    c.getRawText()
            );
        }
    }

    public static PlaceDetailResponse from(Place place, PlaceCondition condition) {
        return new PlaceDetailResponse(
                place.getPlaceId(),
                place.getTitle(),
                place.getCategory(),
                place.getAddress(),
                place.getTel(),
                place.getLatitude(),
                place.getLongitude(),
                place.getThumbnail(),
                ConditionResponse.from(condition)
        );
    }
}