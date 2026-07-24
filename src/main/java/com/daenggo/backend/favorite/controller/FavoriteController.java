package com.daenggo.backend.favorite.controller;

import com.daenggo.backend.favorite.dto.FavoriteResponse;
import com.daenggo.backend.favorite.service.FavoriteService;
import com.daenggo.backend.place.util.AuthUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** 장소 찜 API */
@RestController
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    /** 찜 등록 */
    @PostMapping("/api/places/{placeId}/favorites")
    public Long addFavorite(@PathVariable Long placeId,
                            @AuthenticationPrincipal Jwt jwt) {
        return favoriteService.addFavorite(AuthUtil.userId(jwt), placeId);
    }

    /** 찜 해제 */
    @DeleteMapping("/api/places/{placeId}/favorites")
    public void removeFavorite(@PathVariable Long placeId,
                               @AuthenticationPrincipal Jwt jwt) {
        favoriteService.removeFavorite(AuthUtil.userId(jwt), placeId);
    }

    /** 내 찜 목록 */
    @GetMapping("/api/users/me/favorites")
    public List<FavoriteResponse> getMyFavorites(@AuthenticationPrincipal Jwt jwt) {
        return favoriteService.getMyFavorites(AuthUtil.userId(jwt));
    }

    /** 찜 확인 처리 (정보 변경 배지 제거) */
    @PatchMapping("/api/places/{placeId}/favorites/check")
    public void checkFavorite(@PathVariable Long placeId,
                              @AuthenticationPrincipal Jwt jwt) {
        favoriteService.checkFavorite(AuthUtil.userId(jwt), placeId);
    }
}