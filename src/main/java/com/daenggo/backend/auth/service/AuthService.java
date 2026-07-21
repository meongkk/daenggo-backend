package com.daenggo.backend.auth.service;

import com.daenggo.backend.auth.dto.AuthRequestDto;
import com.daenggo.backend.auth.dto.AuthResponseDto;
import com.daenggo.backend.user.entity.AuthProvider;
import com.daenggo.backend.user.entity.User;
import com.daenggo.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;

/**
 * 로컬 회원 인증 비즈니스 로직
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private static final String TOKEN_TYPE = "Bearer";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AccessTokenService accessTokenService;
    private final RefreshTokenService refreshTokenService;

    /**
     * 로컬 회원가입 처리
     *
     * @param request 회원가입 요청
     * @return 가입 회원 정보
     */
    @Transactional
    public AuthResponseDto.Signup signup(final AuthRequestDto.Signup request) {
        final String email = normalizeEmail(request.getEmail());
        final String nickname = request.getNickname().strip();

        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "이미 가입된 이메일입니다."
            );
        }

        if (userRepository.existsByNicknameAndDeletedAtIsNull(nickname)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "이미 사용 중인 닉네임입니다."
            );
        }

        final User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode(request.getPassword()))
                .nickname(nickname)
                .image(request.getProfileImageUrl())
                .provider(AuthProvider.LOCAL)
                .build();

        return AuthResponseDto.Signup.from(userRepository.save(user));
    }

    /**
     * 이메일 사용 가능 여부 확인
     *
     * @param email 확인 이메일
     * @return 이메일 사용 가능 여부
     */
    public AuthResponseDto.Availability checkEmail(final String email) {
        return new AuthResponseDto.Availability(
                !userRepository.existsByEmailIgnoreCase(normalizeEmail(email))
        );
    }

    /**
     * 닉네임 사용 가능 여부 확인
     *
     * @param nickname 확인 닉네임
     * @return 닉네임 사용 가능 여부
     */
    public AuthResponseDto.Availability checkNickname(final String nickname) {
        return new AuthResponseDto.Availability(
                !userRepository.existsByNicknameAndDeletedAtIsNull(nickname.strip())
        );
    }

    /**
     * 로컬 로그인 및 토큰 발급
     *
     * @param request 로그인 요청
     * @return Access Token과 Refresh Token
     */
    @Transactional
    public AuthResponseDto.Token login(final AuthRequestDto.Login request) {
        final User user = userRepository.findByEmailIgnoreCaseAndDeletedAtIsNull(
                        normalizeEmail(request.getEmail())
                )
                .filter(foundUser -> foundUser.getProvider() == AuthProvider.LOCAL)
                .filter(foundUser -> passwordEncoder.matches(
                        request.getPassword(),
                        foundUser.getPassword()
                ))
                .orElseThrow(this::loginFailed);

        return issueTokenPair(user);
    }

    /**
     * Refresh Token 회전과 Access Token 재발급
     *
     * @param request 토큰 재발급 요청
     * @return 새 Access Token과 Refresh Token
     */
    @Transactional
    public AuthResponseDto.Token reissue(final AuthRequestDto.Refresh request) {
        final RefreshTokenService.RotatedRefreshToken refreshToken =
                refreshTokenService.rotate(request.getRefreshToken());
        final AccessTokenService.IssuedAccessToken accessToken =
                accessTokenService.issue(refreshToken.user());

        return new AuthResponseDto.Token(
                TOKEN_TYPE,
                accessToken.value(),
                accessToken.expiresIn(),
                refreshToken.value(),
                refreshToken.expiresAt()
        );
    }

    /**
     * 로그인 회원 Refresh Token 폐기
     *
     * @param email 로그인 회원 이메일
     * @param request 로그아웃 요청
     */
    @Transactional
    public void logout(final String email, final AuthRequestDto.Refresh request) {
        final User user = userRepository.findByEmailIgnoreCaseAndDeletedAtIsNull(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "인증된 회원을 찾을 수 없습니다."
                ));
        refreshTokenService.revokeOwnedToken(user, request.getRefreshToken());
    }

    private AuthResponseDto.Token issueTokenPair(final User user) {
        final AccessTokenService.IssuedAccessToken accessToken = accessTokenService.issue(user);
        final RefreshTokenService.IssuedRefreshToken refreshToken = refreshTokenService.issue(user);

        return new AuthResponseDto.Token(
                TOKEN_TYPE,
                accessToken.value(),
                accessToken.expiresIn(),
                refreshToken.value(),
                refreshToken.expiresAt()
        );
    }

    private String normalizeEmail(final String email) {
        return email.strip().toLowerCase(Locale.ROOT);
    }

    private ResponseStatusException loginFailed() {
        return new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "이메일 또는 비밀번호가 일치하지 않습니다."
        );
    }
}
