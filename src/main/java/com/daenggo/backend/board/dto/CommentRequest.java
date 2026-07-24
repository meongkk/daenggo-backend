package com.daenggo.backend.board.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 댓글 등록·수정 요청의 내용만 담는다. 작성자는 JWT에서 결정한다. */
@Getter
@NoArgsConstructor
public class CommentRequest {

    /** 댓글 내용. */
    @NotBlank(message = "댓글 내용을 입력해주세요.")
    private String content;
}
