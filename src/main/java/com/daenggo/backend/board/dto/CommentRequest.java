package com.daenggo.backend.board.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 댓글 등록에 필요한 요청 데이터.
 */
@Getter
@NoArgsConstructor
public class CommentRequest {

    /** 댓글 내용. */
    @NotBlank(message = "댓글 내용을 입력해주세요.")
    private String content;

    /** 댓글 작성자 식별자. */
    @NotNull(message = "댓글 작성자 ID를 입력해주세요.")
    @Positive(message = "댓글 작성자 ID는 양수여야 합니다.")
    private Long userId;
}
