package com.daenggo.backend.auth.service;

import com.daenggo.backend.auth.entity.RefreshToken;
import com.daenggo.backend.auth.repository.RefreshTokenRepository;
import com.daenggo.backend.user.entity.AuthProvider;
import com.daenggo.backend.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    private static final Instant NOW = Instant.parse("2026-07-21T00:00:00Z");

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    private RefreshTokenService refreshTokenService;

    @BeforeEach
    void setUp() {
        refreshTokenService = new RefreshTokenService(
                refreshTokenRepository,
                new SecureRandom(),
                Clock.fixed(NOW, ZoneOffset.UTC),
                14
        );
    }

    @Test
    void issueStoresOnlyTokenHash() {
        final User user = createUser(1L);
        final ArgumentCaptor<RefreshToken> tokenCaptor =
                ArgumentCaptor.forClass(RefreshToken.class);

        final RefreshTokenService.IssuedRefreshToken issued =
                refreshTokenService.issue(user);

        verify(refreshTokenRepository).save(tokenCaptor.capture());
        final RefreshToken savedToken = tokenCaptor.getValue();
        assertThat(issued.value()).hasSize(43);
        assertThat(savedToken.getTokenHash()).hasSize(64);
        assertThat(savedToken.getTokenHash()).isNotEqualTo(issued.value());
        assertThat(issued.expiresAt()).isEqualTo(NOW.plus(14, ChronoUnit.DAYS));
    }

    @Test
    void rotateReplacesStoredHashAndReturnsNewToken() {
        final User user = createUser(1L);
        final RefreshToken storedToken = RefreshToken.issue(
                user,
                "old-hash",
                NOW.plus(1, ChronoUnit.DAYS)
        );
        given(refreshTokenRepository.findActiveByTokenHashForUpdate(anyString()))
                .willReturn(Optional.of(storedToken));

        final RefreshTokenService.RotatedRefreshToken rotated =
                refreshTokenService.rotate("old-refresh-token");

        assertThat(rotated.user()).isSameAs(user);
        assertThat(rotated.value()).hasSize(43);
        assertThat(storedToken.getTokenHash()).hasSize(64);
        assertThat(storedToken.getTokenHash()).isNotEqualTo("old-hash");
        assertThat(storedToken.getExpiresAt()).isEqualTo(NOW.plus(14, ChronoUnit.DAYS));
    }

    @Test
    void rotateRejectsExpiredToken() {
        final RefreshToken storedToken = RefreshToken.issue(
                createUser(1L),
                "old-hash",
                NOW.minusSeconds(1)
        );
        given(refreshTokenRepository.findActiveByTokenHashForUpdate(anyString()))
                .willReturn(Optional.of(storedToken));

        assertThatThrownBy(() -> refreshTokenService.rotate("expired-refresh-token"))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(exception -> assertThat(
                        ((ResponseStatusException) exception).getStatusCode()
                ).isEqualTo(HttpStatus.UNAUTHORIZED));
    }

    @Test
    void revokeOwnedTokenRejectsAnotherUsersToken() {
        final User requester = createUser(1L);
        final RefreshToken storedToken = RefreshToken.issue(
                createUser(2L),
                "stored-hash",
                NOW.plus(1, ChronoUnit.DAYS)
        );
        given(refreshTokenRepository.findByTokenHashAndRevokedAtIsNull(anyString()))
                .willReturn(Optional.of(storedToken));

        assertThatThrownBy(() -> refreshTokenService.revokeOwnedToken(
                requester,
                "another-users-token"
        ))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(exception -> assertThat(
                        ((ResponseStatusException) exception).getStatusCode()
                ).isEqualTo(HttpStatus.UNAUTHORIZED));
    }

    @Test
    void revokeAllDelegatesWithCurrentTime() {
        refreshTokenService.revokeAll(7L);

        verify(refreshTokenRepository).revokeAllByUserId(7L, NOW);
    }

    private User createUser(final Long id) {
        final User user = User.builder()
                .email("member" + id + "@daenggo.com")
                .password("encoded-password")
                .nickname("회원" + id)
                .provider(AuthProvider.LOCAL)
                .build();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }
}
