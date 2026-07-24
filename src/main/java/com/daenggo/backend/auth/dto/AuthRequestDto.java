package com.daenggo.backend.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 인증 요청 데이터 모음
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AuthRequestDto {

    /**
     * 로컬 회원가입 요청
     */
    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class Signup {

        @NotBlank
        @Email
        @Size(max = 100)
        private String email;

        @NotBlank
        @Size(min = 8, max = 72)
        private String password;

        @NotBlank
        @Size(max = 50)
        private String nickname;

        @Size(max = 250)
        private String profileImageUrl;
    }

    /**
     * 로컬 로그인 요청
     */
    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class Login {

        @NotBlank
//        @Email// 로그인할때 @없으면 로그인이 안됨
        @Size(max = 100)
        private String email;

        @NotBlank
        private String password;
    }

    /**
     * Refresh Token 처리 요청
     */
    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class Refresh {

        @NotBlank
        private String refreshToken;
    }
}
