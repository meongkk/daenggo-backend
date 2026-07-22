package com.daenggo.backend.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Clock;

/**
 * JWT 서명·검증 구성
 */
@Configuration
public class JwtConfig {

    private static final int MINIMUM_SECRET_BYTE_LENGTH = 32;

    /**
     * HMAC SHA-256 JWT 비밀키 생성
     *
     * @param secret 환경변수 기반 비밀키
     * @return JWT 비밀키
     */
    @Bean
    public SecretKey jwtSecretKey(@Value("${auth.jwt.secret}") final String secret) {
        final byte[] secretBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (secretBytes.length < MINIMUM_SECRET_BYTE_LENGTH) {
            throw new IllegalStateException("JWT_SECRET은 32바이트 이상이어야 합니다.");
        }
        return new SecretKeySpec(secretBytes, "HmacSHA256");
    }

    /**
     * HMAC SHA-256 JWT 인코더 생성
     *
     * @param secretKey JWT 비밀키
     * @return JWT 인코더
     */
    @Bean
    public JwtEncoder jwtEncoder(final SecretKey secretKey) {
        return NimbusJwtEncoder.withSecretKey(secretKey)
                .algorithm(MacAlgorithm.HS256)
                .build();
    }

    /**
     * 발급자와 토큰 유형을 검증하는 JWT 디코더 생성
     *
     * @param secretKey JWT 비밀키
     * @param issuer 허용 토큰 발급자
     * @return JWT 디코더
     */
    @Bean
    public JwtDecoder jwtDecoder(
            final SecretKey secretKey,
            @Value("${auth.jwt.issuer}") final String issuer
    ) {
        final NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(secretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
        final OAuth2TokenValidator<Jwt> issuerValidator =
                JwtValidators.createDefaultWithIssuer(issuer);
        final OAuth2TokenValidator<Jwt> tokenTypeValidator =
                new JwtClaimValidator<>("type", "access"::equals);
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                issuerValidator,
                tokenTypeValidator
        ));
        return decoder;
    }

    /**
     * UTC 기준 시계 생성
     *
     * @return UTC 기준 시계
     */
    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

    /**
     * Refresh Token용 보안 난수 생성기 생성
     *
     * @return 보안 난수 생성기
     */
    @Bean
    public SecureRandom secureRandom() {
        return new SecureRandom();
    }
}
