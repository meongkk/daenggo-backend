package com.daenggo.backend.auth.service;

import com.daenggo.backend.auth.dto.AuthRequestDto;
import com.daenggo.backend.auth.dto.AuthResponseDto;
import com.daenggo.backend.user.entity.AuthProvider;
import com.daenggo.backend.user.entity.User;
import com.daenggo.backend.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private static final String EMAIL = "member@daenggo.com";
    private static final Instant REFRESH_TOKEN_EXPIRES_AT =
            Instant.parse("2026-08-04T00:00:00Z");

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AccessTokenService accessTokenService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private AuthRequestDto.Signup signupRequest;

    @Mock
    private AuthRequestDto.Login loginRequest;

    @Mock
    private AuthRequestDto.Refresh refreshRequest;

    @InjectMocks
    private AuthService authService;

    @Test
    void signupStoresNormalizedEmailAndEncodedPassword() {
        given(signupRequest.getEmail()).willReturn(" MEMBER@DAENGGO.COM ");
        given(signupRequest.getPassword()).willReturn("plain-password");
        given(signupRequest.getNickname()).willReturn(" 댕고회원 ");
        given(signupRequest.getProfileImageUrl()).willReturn("https://images.daenggo.com/me.png");
        given(userRepository.existsByEmailIgnoreCase(EMAIL)).willReturn(false);
        given(userRepository.existsByNicknameAndDeletedAtIsNull("댕고회원")).willReturn(false);
        given(passwordEncoder.encode("plain-password")).willReturn("encoded-password");
        given(userRepository.save(any(User.class))).willAnswer(invocation -> invocation.getArgument(0));

        final AuthResponseDto.Signup response = authService.signup(signupRequest);

        assertThat(response.getEmail()).isEqualTo(EMAIL);
        assertThat(response.getNickname()).isEqualTo("댕고회원");
        verify(passwordEncoder).encode("plain-password");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void signupRejectsDuplicateEmail() {
        given(signupRequest.getEmail()).willReturn(EMAIL);
        given(signupRequest.getNickname()).willReturn("댕고회원");
        given(userRepository.existsByEmailIgnoreCase(EMAIL)).willReturn(true);

        assertThatThrownBy(() -> authService.signup(signupRequest))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(exception -> assertThat(
                        ((ResponseStatusException) exception).getStatusCode()
                ).isEqualTo(HttpStatus.CONFLICT));

        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void checkEmailNormalizesInput() {
        given(userRepository.existsByEmailIgnoreCase(EMAIL)).willReturn(false);

        final AuthResponseDto.Availability response =
                authService.checkEmail(" MEMBER@DAENGGO.COM ");

        assertThat(response.isAvailable()).isTrue();
    }

    @Test
    void loginIssuesAccessAndRefreshTokens() {
        final User user = createLocalUser();
        given(loginRequest.getEmail()).willReturn(EMAIL);
        given(loginRequest.getPassword()).willReturn("plain-password");
        given(userRepository.findByEmailIgnoreCaseAndDeletedAtIsNull(EMAIL))
                .willReturn(Optional.of(user));
        given(passwordEncoder.matches("plain-password", "encoded-password")).willReturn(true);
        given(accessTokenService.issue(user))
                .willReturn(new AccessTokenService.IssuedAccessToken("access-token", 900));
        given(refreshTokenService.issue(user))
                .willReturn(new RefreshTokenService.IssuedRefreshToken(
                        "refresh-token",
                        REFRESH_TOKEN_EXPIRES_AT
                ));

        final AuthResponseDto.Token response = authService.login(loginRequest);

        assertThat(response.getTokenType()).isEqualTo("Bearer");
        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getAccessTokenExpiresIn()).isEqualTo(900);
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
    }

    @Test
    void loginRejectsWrongPasswordWithoutRevealingAccountState() {
        final User user = createLocalUser();
        given(loginRequest.getEmail()).willReturn(EMAIL);
        given(loginRequest.getPassword()).willReturn("wrong-password");
        given(userRepository.findByEmailIgnoreCaseAndDeletedAtIsNull(EMAIL))
                .willReturn(Optional.of(user));
        given(passwordEncoder.matches("wrong-password", "encoded-password")).willReturn(false);

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(exception -> {
                    final ResponseStatusException statusException =
                            (ResponseStatusException) exception;
                    assertThat(statusException.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
                    assertThat(statusException.getReason())
                            .isEqualTo("이메일 또는 비밀번호가 일치하지 않습니다.");
                });

        verify(accessTokenService, never()).issue(any());
        verify(refreshTokenService, never()).issue(any());
    }

    @Test
    void reissueRotatesRefreshTokenAndIssuesAccessToken() {
        final User user = createLocalUser();
        given(refreshRequest.getRefreshToken()).willReturn("old-refresh-token");
        given(refreshTokenService.rotate("old-refresh-token"))
                .willReturn(new RefreshTokenService.RotatedRefreshToken(
                        user,
                        "new-refresh-token",
                        REFRESH_TOKEN_EXPIRES_AT
                ));
        given(accessTokenService.issue(user))
                .willReturn(new AccessTokenService.IssuedAccessToken("new-access-token", 900));

        final AuthResponseDto.Token response = authService.reissue(refreshRequest);

        assertThat(response.getAccessToken()).isEqualTo("new-access-token");
        assertThat(response.getRefreshToken()).isEqualTo("new-refresh-token");
        assertThat(response.getRefreshTokenExpiresAt()).isEqualTo(REFRESH_TOKEN_EXPIRES_AT);
    }

    @Test
    void logoutRevokesOnlyAuthenticatedUsersRefreshToken() {
        final User user = createLocalUser();
        given(refreshRequest.getRefreshToken()).willReturn("refresh-token");
        given(userRepository.findByEmailIgnoreCaseAndDeletedAtIsNull(EMAIL))
                .willReturn(Optional.of(user));

        authService.logout(EMAIL, refreshRequest);

        verify(refreshTokenService).revokeOwnedToken(user, "refresh-token");
    }

    private User createLocalUser() {
        return User.builder()
                .email(EMAIL)
                .password("encoded-password")
                .nickname("댕고회원")
                .provider(AuthProvider.LOCAL)
                .build();
    }
}
