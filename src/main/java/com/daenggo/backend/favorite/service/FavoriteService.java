package com.daenggo.backend.favorite.service;

import com.daenggo.backend.favorite.dto.FavoriteResponse;
import com.daenggo.backend.favorite.entity.Favorite;
import com.daenggo.backend.favorite.repository.FavoriteRepository;
import com.daenggo.backend.place.entity.Place;
import com.daenggo.backend.place.repository.PlaceRepository;
import com.daenggo.backend.user.entity.User;
import com.daenggo.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final PlaceRepository placeRepository;
    private final UserRepository userRepository;

    /** 찜 등록 */
    @Transactional
    public Long addFavorite(Long userId, Long placeId) {

        if (favoriteRepository.existsByUser_IdAndPlace_PlaceId(userId, placeId)) {
            throw new IllegalStateException("이미 찜한 장소입니다.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));
        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new IllegalArgumentException("장소를 찾을 수 없습니다."));

        Favorite favorite = Favorite.builder()
                .user(user)
                .place(place)
                .createdAt(LocalDateTime.now())
                .build();

        return favoriteRepository.save(favorite).getFavoriteId();
    }

    /** 찜 해제 */
    @Transactional
    public void removeFavorite(Long userId, Long placeId) {
        Favorite favorite = favoriteRepository
                .findByUser_IdAndPlace_PlaceId(userId, placeId)
                .orElseThrow(() -> new IllegalArgumentException("찜하지 않은 장소입니다."));

        favoriteRepository.delete(favorite);
    }

    /** 내 찜 목록 조회 (최신순) */
    public List<FavoriteResponse> getMyFavorites(Long userId) {
        return favoriteRepository.findByUser_IdOrderByCreatedAtDesc(userId)
                .stream()
                .map(f -> FavoriteResponse.from(f, hasUpdate(f)))
                .toList();
    }

    /** 찜 확인 처리 (정보 변경 배지 제거) */
    @Transactional
    public void checkFavorite(Long userId, Long placeId) {
        Favorite favorite = favoriteRepository
                .findByUser_IdAndPlace_PlaceId(userId, placeId)
                .orElseThrow(() -> new IllegalArgumentException("찜하지 않은 장소입니다."));

        favorite.check();
    }

    /** 마지막 확인 이후 장소 정보가 변경되었는지 판단 */
    private boolean hasUpdate(Favorite favorite) {
        LocalDateTime placeUpdated = favorite.getPlace().getApiModifiedAt();
        if (placeUpdated == null) {
            return false;
        }
        LocalDateTime lastChecked = favorite.getCheckedAt() != null
                ? favorite.getCheckedAt()
                : favorite.getCreatedAt();

        return placeUpdated.isAfter(lastChecked);
    }
}