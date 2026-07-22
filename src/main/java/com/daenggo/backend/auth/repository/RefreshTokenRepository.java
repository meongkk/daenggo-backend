package com.daenggo.backend.auth.repository;

import com.daenggo.backend.auth.entity.RefreshToken;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * Refresh Token 저장소
 */
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select refreshToken
              from RefreshToken refreshToken
              join fetch refreshToken.user
             where refreshToken.token = :token
            """)
    Optional<RefreshToken> findByTokenForUpdate(@Param("token") String token);

    @Modifying
    @Query("""
            delete from RefreshToken refreshToken
             where refreshToken.user.id = :userId
            """)
    int deleteAllByUserId(@Param("userId") Long userId);
}
