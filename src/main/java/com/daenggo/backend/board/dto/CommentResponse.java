package com.daenggo.backend.board.dto;

import com.daenggo.backend.board.entity.Comment;
import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * 화면에 전달할 댓글 데이터.
 */
@Getter
@Builder
public class CommentResponse {

    /** 댓글 식별자. */
    private Long id;

    /** 댓글 내용. */
    private String content;

    /** 댓글 작성자 닉네임. */
    private String nickname;

    /** 프론트엔드가 현재 사용자의 댓글인지 판단할 때 사용하는 작성자 ID. */
    private Long userId;

    /** 댓글 등록 시각. */
    private OffsetDateTime createdAt;

    /** 댓글을 마지막으로 수정한 시각. */
    private OffsetDateTime updatedAt;

    /**
     * 댓글 엔티티를 응답 데이터로 변환한다.
     *
     * @param comment 변환할 댓글 엔티티
     * @return 화면에 전달할 댓글 응답
     */
    public static CommentResponse from(Comment comment) {
        return CommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .nickname(comment.getUser().getNickname())
                .userId(comment.getUser().getId())
                .createdAt(comment.getCreatedAt() == null
                        ? null
                        : comment.getCreatedAt().atOffset(ZoneOffset.UTC))
                .updatedAt(comment.getUpdatedAt() == null
                        ? null
                        : comment.getUpdatedAt().atOffset(ZoneOffset.UTC))
                .build();
    }
}
