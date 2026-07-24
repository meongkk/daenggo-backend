package com.daenggo.backend.place.util;

import org.springframework.security.oauth2.jwt.Jwt;

/**
 * 인증 토큰에서 사용자 정보를 추출하는 유틸리티
 *
 * 토큰의 subject는 email이며, userId는 별도 클레임으로 담겨 있다.
 */
public final class AuthUtil {

    private AuthUtil() {
    }

    /** JWT에서 사용자 ID를 추출한다 */
    public static Long userId(Jwt jwt) {
        if (jwt == null) {
            throw new IllegalStateException("인증 정보가 없습니다.");
        }
        Object claim = jwt.getClaim("userId");
        if (claim == null) {
            throw new IllegalStateException("토큰에 사용자 정보가 없습니다.");
        }
        return ((Number) claim).longValue();
    }
}