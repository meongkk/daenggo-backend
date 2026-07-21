package com.daenggo.backend.auth.service;

import com.daenggo.backend.auth.entity.RefreshToken;
import com.daenggo.backend.auth.repository.RefreshTokenRepository;
import com.daenggo.backend.user.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Objects;

/**
 * Refresh Token 생성·회전·폐기 서비스
 */
@Service
@Transactional(readOnly = true)
public class RefreshTokenService {

    private static final int TOKEN_BYTE_LENGTH = 32;

    private final RefreshTokenRepository refreshTokenRepository;
    private final SecureRandom secureRandom;
    private final Clock clock;
    private final long expirationDays;

    /**
     * Refresh Token 서비스 생성
     *
     * @param refreshTokenRepository Refresh Token 저장소
     * @param secureRandom 보안 난수 생성기
     * @param clock 기준 시계
     * @param expirationDays Refresh Token 만료 일수
     */
    public RefreshTokenService(
            final RefreshTokenRepository refreshTokenRepository,
            final SecureRandom secureRandom,
            final Clock clock,
            @Value("${auth.refresh-token-expiration-days}") final long expirationDays
    ) {
        if (expirationDays <= 0) {
            throw new IllegalArgumentException("Refresh Token 만료 일수는 0보다 커야 합니다.");
        }

        this.refreshTokenRepository = refreshTokenRepository;
        this.secureRandom = secureRandom;
        this.clock = clock;
        this.expirationDays = expirationDays;
    }

    /**
     * 회원용 신규 Refresh Token 발급
     *
     * @param user 토큰 소유 회원
     * @return 발급된 Refresh Token
     */
    @Transactional
    public IssuedRefreshToken issue(final User user) {
        final String tokenValue = generateTokenValue();
        final Instant expiresAt = expirationFromNow();

        refreshTokenRepository.save(RefreshToken.issue(
                user,
                hash(tokenValue),
                expiresAt
        ));

        return new IssuedRefreshToken(tokenValue, expiresAt);
    }

    /**
     * 기존 Refresh Token 검증 및 회전
     *
     * @param tokenValue 기존 Refresh Token 원문
     * @return 회전된 Refresh Token과 회원 정보
     */
    @Transactional
    public RotatedRefreshToken rotate(final String tokenValue) {
        final RefreshToken refreshToken = findActiveToken(tokenValue);
        final Instant now = clock.instant();

        if (refreshToken.isExpiredAt(now) || refreshToken.getUser().getDeletedAt() != null) {
            throw invalidRefreshToken();
        }

        final String rotatedTokenValue = generateTokenValue();
        final Instant expiresAt = expirationFromNow();
        refreshToken.rotate(hash(rotatedTokenValue), expiresAt);

        return new RotatedRefreshToken(
                refreshToken.getUser(),
                rotatedTokenValue,
                expiresAt
        );
    }

    /**
     * 회원 소유 Refresh Token 폐기
     *
     * @param user 요청 회원
     * @param tokenValue 폐기할 Refresh Token 원문
     */
    @Transactional
    public void revokeOwnedToken(final User user, final String tokenValue) {
        refreshTokenRepository.findByTokenHashAndRevokedAtIsNull(hash(tokenValue))
                .ifPresent(refreshToken -> {
                    if (!Objects.equals(refreshToken.getUser().getId(), user.getId())) {
                        throw invalidRefreshToken();
                    }
                    refreshToken.revoke(clock.instant());
                });
    }

    /**
     * 회원의 모든 Refresh Token 폐기
     *
     * @param userId 회원 식별자
     */
    @Transactional
    public void revokeAll(final Long userId) {
        refreshTokenRepository.revokeAllByUserId(userId, clock.instant());
    }

    private RefreshToken findActiveToken(final String tokenValue) {
        return refreshTokenRepository.findActiveByTokenHashForUpdate(hash(tokenValue))
                .orElseThrow(this::invalidRefreshToken);
    }

    private String generateTokenValue() {
        final byte[] tokenBytes = new byte[TOKEN_BYTE_LENGTH];
        secureRandom.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }

    private Instant expirationFromNow() {
        return clock.instant().plus(expirationDays, ChronoUnit.DAYS);
    }

    private String hash(final String tokenValue) {
        try {
            final MessageDigest digest = MessageDigest.getInstance("SHA-256");
            final byte[] hash = digest.digest(tokenValue.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 알고리즘을 사용할 수 없습니다.", exception);
        }
    }

    private ResponseStatusException invalidRefreshToken() {
        return new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "유효하지 않은 Refresh Token입니다."
        );
    }

    /**
     * 발급된 Refresh Token 정보
     *
     * @param value 토큰 원문
     * @param expiresAt 토큰 만료 시각
     */
    public record IssuedRefreshToken(String value, Instant expiresAt) {
    }

    /**
     * 회전된 Refresh Token 정보
     *
     * @param user 토큰 소유 회원
     * @param value 토큰 원문
     * @param expiresAt 토큰 만료 시각
     */
    public record RotatedRefreshToken(User user, String value, Instant expiresAt) {
    }
}
