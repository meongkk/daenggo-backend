package com.daenggo.backend.board.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class BoardUpdateRequest {
    @NotBlank(message = "수정할 제목을 입력해주세요.")
    private String title;

    @NotBlank(message = "수정할 내용을 입력해주세요.")
    private String content;
}