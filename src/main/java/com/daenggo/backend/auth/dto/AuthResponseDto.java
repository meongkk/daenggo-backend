package com.daenggo.backend.auth.dto;

import com.daenggo.backend.user.entity.User;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 인증 응답 데이터 모음
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AuthResponseDto {

    /**
     * 회원가입 결과
     */
    @Getter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Signup {

        private final Long userId;
        private final String email;
        private final String nickname;

        /**
         * 회원 엔티티 기반 회원가입 응답 생성
         *
         * @param user 가입 회원
         * @return 회원가입 응답
         */
        public static Signup from(final User user) {
            return new Signup(user.getId(), user.getEmail(), user.getNickname());
        }
    }

    /**
     * 입력값 사용 가능 여부
     */
    @Getter
    @AllArgsConstructor
    public static class Availability {

        private final boolean available;
    }

    /**
     * Access Token과 Refresh Token 발급 결과
     */
    @Getter
    @AllArgsConstructor
    public static class Token {

        private final String tokenType;
        private final String accessToken;
        private final long accessTokenExpiresIn;
        private final String refreshToken;
    }
}
