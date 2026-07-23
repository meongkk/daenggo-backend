package com.daenggo.backend.auth.service;

import com.daenggo.backend.global.config.JwtConfig;
import com.daenggo.backend.user.entity.AuthProvider;
import com.daenggo.backend.user.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.time.Clock;

import static org.assertj.core.api.Assertions.assertThat;

class AccessTokenServiceTest {

    @Test
    void issuedTokenCanBeVerifiedByConfiguredDecoder() {
        final JwtConfig jwtConfig = new JwtConfig();
        final SecretKey secretKey = jwtConfig.jwtSecretKey(
                "0123456789abcdef0123456789abcdef"
        );
        final JwtEncoder encoder = jwtConfig.jwtEncoder(secretKey);
        final JwtDecoder decoder = jwtConfig.jwtDecoder(secretKey, "daenggo-api");
        final AccessTokenService accessTokenService = new AccessTokenService(
                encoder,
                Clock.systemUTC(),
                "daenggo-api",
                900
        );
        final User user = User.builder()
                .email("member@daenggo.com")
                .password("encoded-password")
                .nickname("댕고회원")
                .provider(AuthProvider.LOCAL)
                .build();
        ReflectionTestUtils.setField(user, "id", 7L);

        final AccessTokenService.IssuedAccessToken issued = accessTokenService.issue(user);
        final Jwt decoded = decoder.decode(issued.value());

        assertThat(decoded.getSubject()).isEqualTo("member@daenggo.com");
        assertThat(decoded.getClaimAsString("iss")).isEqualTo("daenggo-api");
        assertThat(decoded.getClaimAsString("type")).isEqualTo("access");
        assertThat(decoded.getClaimAsString("role")).isEqualTo("MEMBER");
        assertThat(((Number) decoded.getClaim("userId")).longValue()).isEqualTo(7L);
        assertThat(issued.expiresIn()).isEqualTo(900);
    }
}
