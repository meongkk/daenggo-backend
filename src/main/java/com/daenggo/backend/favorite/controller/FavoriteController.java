package com.daenggo.backend.favorite.controller;

import com.daenggo.backend.favorite.dto.FavoriteResponse;
import com.daenggo.backend.favorite.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    /** 찜 등록 */
    @PostMapping("/api/places/{placeId}/favorites")
    public Long addFavorite(@PathVariable Long placeId,
                            @RequestParam Long userId) {   // TODO: 인증 정보에서 추출로 변경
        return favoriteService.addFavorite(userId, placeId);
    }

    /** 찜 해제 */
    @DeleteMapping("/api/places/{placeId}/favorites")
    public void removeFavorite(@PathVariable Long placeId,
                               @RequestParam Long userId) {   // TODO
        favoriteService.removeFavorite(userId, placeId);
    }

    /** 내 찜 목록 */
    @GetMapping("/api/users/me/favorites")
    public List<FavoriteResponse> getMyFavorites(@RequestParam Long userId) {   // TODO
        return favoriteService.getMyFavorites(userId);
    }

    /** 찜 확인 처리 */
    @PatchMapping("/api/places/{placeId}/favorites/check")
    public void checkFavorite(@PathVariable Long placeId,
                              @RequestParam Long userId) {   // TODO
        favoriteService.checkFavorite(userId, placeId);
    }
}