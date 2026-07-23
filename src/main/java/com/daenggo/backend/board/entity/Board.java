package com.daenggo.backend.board.entity;

import com.daenggo.backend.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Entity
@Table(name = "board")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Board {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;


    @Column(name = "type", nullable = false, length = 50)
    private String type;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "view_count", nullable = false)
    private Integer viewCount = 0;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Builder
    public Board(String type, String title, String content ,User user) {
        this.type = type;
        this.title = title;
        this.content = content;
        this.user = user;
    }

    /**
     * 게시글을 처음 저장하기 직전에 작성 시각을 UTC 기준으로 기록한다.
     * API가 명확한 시간대를 포함해 응답할 수 있도록 서버 실행 환경과 관계없이 같은 기준을 사용한다.
     */
    @PrePersist
    private void recordCreatedAt() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now(ZoneOffset.UTC);
        }
    }

    /**
     * 게시글 상세가 조회될 때 현재 조회수를 1 증가시킨다.
     */
    public void increaseViewCount() {
        this.viewCount += 1;
    }

    /**
     * 게시글 수정
     * @param title
     * @param content
     */
    public void update(String title, String content) {
        this.title = title;
        this.content = content;
    }

    /**
     * 게시글 삭제
     */
    public void deleteSoftly() {
        this.deletedAt = LocalDateTime.now(); // 현재 시간을 삭제 시간으로 기록
    }
}
