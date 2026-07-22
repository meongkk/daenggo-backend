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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    private RefreshTokenService refreshTokenService;

    @BeforeEach
    void setUp() {
        refreshTokenService = new RefreshTokenService(
                refreshTokenRepository,
                new SecureRandom()
        );
    }

    @Test
    void issueStoresIssuedToken() {
        final User user = createUser(1L);
        final ArgumentCaptor<RefreshToken> tokenCaptor =
                ArgumentCaptor.forClass(RefreshToken.class);

        final RefreshTokenService.IssuedRefreshToken issued =
                refreshTokenService.issue(user);

        verify(refreshTokenRepository).save(tokenCaptor.capture());
        final RefreshToken savedToken = tokenCaptor.getValue();
        assertThat(issued.value()).hasSize(43);
        assertThat(savedToken.getToken()).isEqualTo(issued.value());
        assertThat(savedToken.getUser()).isSameAs(user);
    }

    @Test
    void rotateReplacesStoredTokenAndReturnsNewToken() {
        final User user = createUser(1L);
        final RefreshToken storedToken = RefreshToken.issue(user, "old-refresh-token");
        given(refreshTokenRepository.findByTokenForUpdate("old-refresh-token"))
                .willReturn(Optional.of(storedToken));

        final RefreshTokenService.RotatedRefreshToken rotated =
                refreshTokenService.rotate("old-refresh-token");

        assertThat(rotated.user()).isSameAs(user);
        assertThat(rotated.value()).hasSize(43);
        assertThat(rotated.value()).isNotEqualTo("old-refresh-token");
        assertThat(storedToken.getToken()).isEqualTo(rotated.value());
    }

    @Test
    void rotateRejectsWithdrawnUsersToken() {
        final User user = createUser(1L);
        user.withdraw();
        final RefreshToken storedToken = RefreshToken.issue(user, "refresh-token");
        given(refreshTokenRepository.findByTokenForUpdate("refresh-token"))
                .willReturn(Optional.of(storedToken));

        assertThatThrownBy(() -> refreshTokenService.rotate("refresh-token"))
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
                "another-users-token"
        );
        given(refreshTokenRepository.findByToken("another-users-token"))
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
    void revokeOwnedTokenDeletesOwnedToken() {
        final User user = createUser(1L);
        final RefreshToken storedToken = RefreshToken.issue(user, "refresh-token");
        given(refreshTokenRepository.findByToken("refresh-token"))
                .willReturn(Optional.of(storedToken));

        refreshTokenService.revokeOwnedToken(user, "refresh-token");

        verify(refreshTokenRepository).delete(storedToken);
    }

    @Test
    void revokeAllDeletesUsersTokens() {
        refreshTokenService.revokeAll(7L);

        verify(refreshTokenRepository).deleteAllByUserId(7L);
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
