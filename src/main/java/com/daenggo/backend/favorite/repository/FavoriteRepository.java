// favorite/repository/FavoriteRepository.java
package com.daenggo.backend.favorite.repository;

import com.daenggo.backend.favorite.entity.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    boolean existsByUser_IdAndPlace_PlaceId(Long userId, Long placeId);

    Optional<Favorite> findByUser_IdAndPlace_PlaceId(Long userId, Long placeId);

    List<Favorite> findByUser_IdOrderByCreatedAtDesc(Long userId);
}