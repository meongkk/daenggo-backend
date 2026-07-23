package com.daenggo.backend.favorite.dto;

import com.daenggo.backend.favorite.entity.Favorite;
import java.math.BigDecimal;

public record FavoriteResponse(
        Long favoriteId,
        Long placeId,
        String title,
        String category,
        String thumbnail,
        BigDecimal latitude,
        BigDecimal longitude,
        boolean hasUpdate      // 규정 변경 여부
) {
    public static FavoriteResponse from(Favorite favorite, boolean hasUpdate) {
        var place = favorite.getPlace();
        return new FavoriteResponse(
                favorite.getFavoriteId(),
                place.getPlaceId(),
                place.getTitle(),
                place.getCategory(),
                place.getThumbnail(),
                place.getLatitude(),
                place.getLongitude(),
                hasUpdate
        );
    }
}