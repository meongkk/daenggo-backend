package com.daenggo.backend.user.service;

import com.daenggo.backend.auth.service.RefreshTokenService;
import com.daenggo.backend.user.dto.UserRequestDto;
import com.daenggo.backend.user.dto.UserResponseDto;
import com.daenggo.backend.user.entity.AuthProvider;
import com.daenggo.backend.user.entity.User;
import com.daenggo.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * 회원 정보 관리 비즈니스 로직
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;

    /**
     * 로그인 회원 정보 조회
     *
     * @param email 로그인 회원 이메일
     * @return 로그인 회원 정보
     */
    public UserResponseDto.Me getMyInfo(final String email) {
        final User user = findActiveUser(email);
        return UserResponseDto.Me.from(user);
    }

    /**
     * 로그인 회원 정보 수정
     *
     * @param email 로그인 회원 이메일
     * @param request 회원 정보 수정 요청
     * @return 수정된 회원 정보
     */
    @Transactional
    public UserResponseDto.Me updateMyInfo(
            final String email,
            final UserRequestDto.Update request
    ) {
        final User user = findActiveUser(email);

        validateCurrentPassword(request.getCurrentPassword(), user);
        validateNickname(request.getNickname(), user);
        user.updateProfile(request.getNickname(), request.getProfileImageUrl());

        return UserResponseDto.Me.from(user);
    }

    /**
     * 로그인 회원 비밀번호 변경
     *
     * @param email 로그인 회원 이메일
     * @param request 비밀번호 변경 요청
     */
    @Transactional
    public void changePassword(
            final String email,
            final UserRequestDto.PasswordChange request
    ) {
        final User user = findActiveUser(email);

        validatePasswordChangeAllowed(user);
        validateCurrentPassword(request.getCurrentPassword(), user);
        user.changePassword(passwordEncoder.encode(request.getNewPassword()));
        refreshTokenService.revokeAll(user.getId());
    }

    /**
     * 로그인 회원 탈퇴 상태 기록
     *
     * @param email 로그인 회원 이메일
     */
    @Transactional
    public void withdraw(final String email) {
        final User user = findActiveUser(email);
        user.withdraw();
        refreshTokenService.revokeAll(user.getId());
    }

    /**
     * 활성 회원 조회
     *
     * @param email 로그인 회원 이메일
     * @return 활성 회원 엔티티
     */
    private User findActiveUser(final String email) {
        return userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "회원을 찾을 수 없습니다."
                ));
    }

    /**
     * 현재 비밀번호 일치 여부 검증
     *
     * @param currentPassword 입력된 현재 비밀번호
     * @param user 검증 대상 회원
     */
    private void validateCurrentPassword(final String currentPassword, final User user) {
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "현재 비밀번호가 일치하지 않습니다."
            );
        }
    }

    /**
     * 로컬 계정의 비밀번호 변경 가능 여부 검증
     *
     * @param user 검증 대상 회원
     */
    private void validatePasswordChangeAllowed(final User user) {
        if (user.getProvider() != AuthProvider.LOCAL) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "소셜 계정은 비밀번호를 변경할 수 없습니다."
            );
        }
    }

    /**
     * 변경 닉네임 유효성 및 중복 여부 검증
     *
     * @param nickname 변경할 닉네임
     * @param user 변경 대상 회원
     */
    private void validateNickname(final String nickname, final User user) {
        if (nickname == null || nickname.equals(user.getNickname())) {
            return;
        }

        if (nickname.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "닉네임은 공백일 수 없습니다."
            );
        }

        if (userRepository.existsByNicknameAndDeletedAtIsNull(nickname)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "이미 사용 중인 닉네임입니다."
            );
        }
    }
}
