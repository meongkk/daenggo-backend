package com.daenggo.backend.user.controller;

import com.daenggo.backend.user.dto.UserRequestDto;
import com.daenggo.backend.user.dto.UserResponseDto;
import com.daenggo.backend.user.service.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 회원 정보 관리 REST 컨트롤러
 */
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    /**
     * 로그인 회원 정보 조회
     *
     * @param authentication 로그인 회원 인증 정보
     * @return 로그인 회원 정보 응답
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponseDto.Me> getMyInfo(final Authentication authentication) {
        final UserResponseDto.Me response = userService.getMyInfo(authentication.getName());
        return ResponseEntity.ok(response);
    }

    /**
     * 로그인 회원 정보 수정
     *
     * @param authentication 로그인 회원 인증 정보
     * @param request 회원 정보 수정 요청
     * @return 수정된 회원 정보 응답
     */
    @PatchMapping("/me")
    public ResponseEntity<UserResponseDto.Me> updateMyInfo(
            final Authentication authentication,
            @Valid @RequestBody final UserRequestDto.Update request
    ) {
        final UserResponseDto.Me response = userService.updateMyInfo(
                authentication.getName(),
                request
        );
        return ResponseEntity.ok(response);
    }

    /**
     * 로그인 회원 비밀번호 변경
     *
     * @param authentication 로그인 회원 인증 정보
     * @param request 비밀번호 변경 요청
     * @return 응답 본문 없는 성공 응답
     */
    @PatchMapping("/me/password")
    public ResponseEntity<Void> changePassword(
            final Authentication authentication,
            @Valid @RequestBody final UserRequestDto.PasswordChange request
    ) {
        userService.changePassword(authentication.getName(), request);
        return ResponseEntity.noContent().build();
    }

    /**
     * 로그인 회원 탈퇴
     *
     * @param authentication 로그인 회원 인증 정보
     * @return 응답 본문 없는 성공 응답
     */
    @DeleteMapping("/me")
    public ResponseEntity<Void> withdraw(final Authentication authentication) {
        userService.withdraw(authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
