package com.daenggo.backend.auth.entity;

import com.daenggo.backend.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 로그인 시 발급되는 Refresh Token 엔티티
 */
@Getter
@Entity
@Table(name = "refresh_tokens")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "token_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "token", nullable = false, length = 255)
    private String token;

    private RefreshToken(final User user, final String token) {
        this.user = user;
        this.token = token;
    }

    /**
     * 신규 Refresh Token 생성
     *
     * @param user 토큰 소유 회원
     * @param token 로그인 시 발급된 Refresh Token
     * @return Refresh Token 엔티티
     */
    public static RefreshToken issue(final User user, final String token) {
        return new RefreshToken(user, token);
    }

    /**
     * Refresh Token 교체
     *
     * @param token 신규 Refresh Token
     */
    public void rotate(final String token) {
        this.token = token;
    }
}
