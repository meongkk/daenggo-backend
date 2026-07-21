package com.daenggo.backend.auth.controller;

import com.daenggo.backend.auth.dto.AuthRequestDto;
import com.daenggo.backend.auth.dto.AuthResponseDto;
import com.daenggo.backend.auth.service.AuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private static final String EMAIL = "member@daenggo.com";

    @Mock
    private AuthService authService;

    @Mock
    private Authentication authentication;

    @Mock
    private AuthRequestDto.Signup signupRequest;

    @Mock
    private AuthRequestDto.Login loginRequest;

    @Mock
    private AuthRequestDto.Refresh refreshRequest;

    @Mock
    private AuthResponseDto.Signup signupResponse;

    @Mock
    private AuthResponseDto.Token tokenResponse;

    @InjectMocks
    private AuthController authController;

    @Test
    void signupReturnsCreated() {
        given(authService.signup(signupRequest)).willReturn(signupResponse);

        final ResponseEntity<AuthResponseDto.Signup> response =
                authController.signup(signupRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isSameAs(signupResponse);
    }

    @Test
    void checkEmailReturnsOk() {
        final AuthResponseDto.Availability availability =
                new AuthResponseDto.Availability(true);
        given(authService.checkEmail(EMAIL)).willReturn(availability);

        final ResponseEntity<AuthResponseDto.Availability> response =
                authController.checkEmail(EMAIL);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(availability);
    }

    @Test
    void loginReturnsTokenPair() {
        given(authService.login(loginRequest)).willReturn(tokenResponse);

        final ResponseEntity<AuthResponseDto.Token> response =
                authController.login(loginRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(tokenResponse);
    }

    @Test
    void reissueReturnsRotatedTokenPair() {
        given(authService.reissue(refreshRequest)).willReturn(tokenResponse);

        final ResponseEntity<AuthResponseDto.Token> response =
                authController.reissue(refreshRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(tokenResponse);
    }

    @Test
    void logoutReturnsNoContent() {
        given(authentication.getName()).willReturn(EMAIL);

        final ResponseEntity<Void> response = authController.logout(
                authentication,
                refreshRequest
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNull();
        verify(authService).logout(EMAIL, refreshRequest);
    }
}
