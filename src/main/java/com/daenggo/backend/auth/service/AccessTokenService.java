package com.daenggo.backend.auth.service;

import com.daenggo.backend.user.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

/**
 * JWT Access Token 발급 서비스
 */
@Service
public class AccessTokenService {

    private static final String TOKEN_TYPE = "access";

    private final JwtEncoder jwtEncoder;
    private final Clock clock;
    private final String issuer;
    private final long expirationSeconds;

    /**
     * Access Token 발급 서비스 생성
     *
     * @param jwtEncoder JWT 인코더
     * @param clock 기준 시계
     * @param issuer 토큰 발급자
     * @param expirationSeconds Access Token 만료 초
     */
    public AccessTokenService(
            final JwtEncoder jwtEncoder,
            final Clock clock,
            @Value("${auth.jwt.issuer}") final String issuer,
            @Value("${auth.jwt.access-token-expiration-seconds}") final long expirationSeconds
    ) {
        if (expirationSeconds <= 0) {
            throw new IllegalArgumentException("Access Token 만료 시간은 0보다 커야 합니다.");
        }

        this.jwtEncoder = jwtEncoder;
        this.clock = clock;
        this.issuer = issuer;
        this.expirationSeconds = expirationSeconds;
    }

    /**
     * 회원 인증용 Access Token 발급
     *
     * @param user 인증 회원
     * @return 발급된 Access Token
     */
    public IssuedAccessToken issue(final User user) {
        final Instant issuedAt = clock.instant();
        final Instant expiresAt = issuedAt.plusSeconds(expirationSeconds);

        final JwtClaimsSet.Builder claimsBuilder = JwtClaimsSet.builder()
                .issuer(issuer)
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .subject(user.getEmail())
                .id(UUID.randomUUID().toString())
                .claim("type", TOKEN_TYPE)
                .claim("role", user.getRole().name());

        if (user.getId() != null) {
            claimsBuilder.claim("userId", user.getId());
        }

        final JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        final String tokenValue = jwtEncoder.encode(
                JwtEncoderParameters.from(header, claimsBuilder.build())
        ).getTokenValue();

        return new IssuedAccessToken(tokenValue, expirationSeconds);
    }

    /**
     * 발급된 Access Token 정보
     *
     * @param value 토큰 원문
     * @param expiresIn 만료까지 남은 초
     */
    public record IssuedAccessToken(String value, long expiresIn) {
    }
}
