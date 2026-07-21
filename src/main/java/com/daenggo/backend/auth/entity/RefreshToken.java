package com.daenggo.backend.auth.entity;

import com.daenggo.backend.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

/**
 * Refresh Token 해시 저장 엔티티
 */
@Getter
@Entity
@Table(
        name = "user_refresh_tokens",
        indexes = @Index(
                name = "uk_user_refresh_tokens_token_hash",
                columnList = "token_hash",
                unique = true
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "refresh_token_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "token_hash", nullable = false, length = 64)
    private String tokenHash;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    private RefreshToken(
            final User user,
            final String tokenHash,
            final Instant expiresAt
    ) {
        this.user = user;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
    }

    /**
     * 신규 Refresh Token 생성
     *
     * @param user 토큰 소유 회원
     * @param tokenHash 토큰 SHA-256 해시
     * @param expiresAt 토큰 만료 시각
     * @return Refresh Token 엔티티
     */
    public static RefreshToken issue(
            final User user,
            final String tokenHash,
            final Instant expiresAt
    ) {
        return new RefreshToken(user, tokenHash, expiresAt);
    }

    /**
     * Refresh Token 회전
     *
     * @param tokenHash 신규 토큰 SHA-256 해시
     * @param expiresAt 신규 토큰 만료 시각
     */
    public void rotate(final String tokenHash, final Instant expiresAt) {
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
        this.revokedAt = null;
    }

    /**
     * Refresh Token 폐기 시각 기록
     *
     * @param revokedAt 토큰 폐기 시각
     */
    public void revoke(final Instant revokedAt) {
        this.revokedAt = revokedAt;
    }

    /**
     * 지정 시각 기준 토큰 만료 여부 확인
     *
     * @param now 기준 시각
     * @return 토큰 만료 여부
     */
    public boolean isExpiredAt(final Instant now) {
        return !expiresAt.isAfter(now);
    }
}
