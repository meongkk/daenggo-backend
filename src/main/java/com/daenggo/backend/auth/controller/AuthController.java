package com.daenggo.backend.auth.controller;

import com.daenggo.backend.auth.dto.AuthRequestDto;
import com.daenggo.backend.auth.dto.AuthResponseDto;
import com.daenggo.backend.auth.service.AuthService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 인증 REST 컨트롤러
 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    /**
     * 로컬 회원가입
     *
     * @param request 회원가입 요청
     * @return 가입 회원 정보 응답
     */
    @PostMapping("/signup")
    public ResponseEntity<AuthResponseDto.Signup> signup(
            @Valid @RequestBody final AuthRequestDto.Signup request
    ) {
        final AuthResponseDto.Signup response = authService.signup(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    /**
     * 이메일 사용 가능 여부 확인
     *
     * @param email 확인 이메일
     * @return 이메일 사용 가능 여부 응답
     */
    @GetMapping("/check-email")
    public ResponseEntity<AuthResponseDto.Availability> checkEmail(
            @RequestParam
            @NotBlank
            @Email
            @Size(max = 100)
            final String email
    ) {
        return ResponseEntity.ok(authService.checkEmail(email));
    }

    /**
     * 닉네임 사용 가능 여부 확인
     *
     * @param nickname 확인 닉네임
     * @return 닉네임 사용 가능 여부 응답
     */
    @GetMapping("/check-nickname")
    public ResponseEntity<AuthResponseDto.Availability> checkNickname(
            @RequestParam
            @NotBlank
            @Size(max = 50)
            final String nickname
    ) {
        return ResponseEntity.ok(authService.checkNickname(nickname));
    }

    /**
     * 로컬 로그인과 토큰 발급
     *
     * @param request 로그인 요청
     * @return Access Token과 Refresh Token 응답
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto.Token> login(
            @Valid @RequestBody final AuthRequestDto.Login request
    ) {
        return ResponseEntity.ok(authService.login(request));
    }

    /**
     * Access Token과 Refresh Token 재발급
     *
     * @param request 토큰 재발급 요청
     * @return 회전된 토큰 응답
     */
    @PostMapping("/reissue")
    public ResponseEntity<AuthResponseDto.Token> reissue(
            @Valid @RequestBody final AuthRequestDto.Refresh request
    ) {
        return ResponseEntity.ok(authService.reissue(request));
    }

    /**
     * 로그인 회원 Refresh Token 폐기
     *
     * @param authentication 로그인 회원 인증 정보
     * @param request 로그아웃 요청
     * @return 응답 본문 없는 성공 응답
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            final Authentication authentication,
            @Valid @RequestBody final AuthRequestDto.Refresh request
    ) {
        authService.logout(authentication.getName(), request);
        return ResponseEntity.noContent().build();
    }
}
