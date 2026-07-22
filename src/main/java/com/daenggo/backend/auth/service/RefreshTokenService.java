package com.daenggo.backend.auth.service;

import com.daenggo.backend.auth.entity.RefreshToken;
import com.daenggo.backend.auth.repository.RefreshTokenRepository;
import com.daenggo.backend.user.entity.User;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Objects;

/**
 * Refresh Token 발급·교체·삭제 서비스
 */
@Service
@Transactional(readOnly = true)
public class RefreshTokenService {

    private static final int TOKEN_BYTE_LENGTH = 32;

    private final RefreshTokenRepository refreshTokenRepository;
    private final SecureRandom secureRandom;

    /**
     * Refresh Token 서비스 생성
     *
     * @param refreshTokenRepository Refresh Token 저장소
     * @param secureRandom 보안 난수 생성기
     */
    public RefreshTokenService(
            final RefreshTokenRepository refreshTokenRepository,
            final SecureRandom secureRandom
    ) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.secureRandom = secureRandom;
    }

    /**
     * 회원의 신규 Refresh Token 발급
     *
     * @param user 토큰 소유 회원
     * @return 발급된 Refresh Token
     */
    @Transactional
    public IssuedRefreshToken issue(final User user) {
        final String tokenValue = generateTokenValue();
        refreshTokenRepository.save(RefreshToken.issue(user, tokenValue));
        return new IssuedRefreshToken(tokenValue);
    }

    /**
     * 기존 Refresh Token 검증 및 교체
     *
     * @param tokenValue 기존 Refresh Token 원문
     * @return 교체된 Refresh Token과 회원 정보
     */
    @Transactional
    public RotatedRefreshToken rotate(final String tokenValue) {
        final RefreshToken refreshToken = refreshTokenRepository.findByTokenForUpdate(tokenValue)
                .orElseThrow(this::invalidRefreshToken);

        if (refreshToken.getUser().getDeletedAt() != null) {
            throw invalidRefreshToken();
        }

        final String rotatedTokenValue = generateTokenValue();
        refreshToken.rotate(rotatedTokenValue);
        return new RotatedRefreshToken(refreshToken.getUser(), rotatedTokenValue);
    }

    /**
     * 회원 소유 Refresh Token 삭제
     *
     * @param user 요청 회원
     * @param tokenValue 삭제할 Refresh Token 원문
     */
    @Transactional
    public void revokeOwnedToken(final User user, final String tokenValue) {
        refreshTokenRepository.findByToken(tokenValue)
                .ifPresent(refreshToken -> {
                    if (!Objects.equals(refreshToken.getUser().getId(), user.getId())) {
                        throw invalidRefreshToken();
                    }
                    refreshTokenRepository.delete(refreshToken);
                });
    }

    /**
     * 회원의 모든 Refresh Token 삭제
     *
     * @param userId 회원 식별자
     */
    @Transactional
    public void revokeAll(final Long userId) {
        refreshTokenRepository.deleteAllByUserId(userId);
    }

    private String generateTokenValue() {
        final byte[] tokenBytes = new byte[TOKEN_BYTE_LENGTH];
        secureRandom.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
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
     */
    public record IssuedRefreshToken(String value) {
    }

    /**
     * 교체된 Refresh Token 정보
     *
     * @param user 토큰 소유 회원
     * @param value 토큰 원문
     */
    public record RotatedRefreshToken(User user, String value) {
    }
}
