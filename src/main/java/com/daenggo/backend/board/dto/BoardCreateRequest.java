package com.daenggo.backend.board.dto;

import jakarta.validation.constraints.NotBlank;

public class BoardCreateRequest {

    @NotBlank(message = "게시판 종류를 선택해주세요.")
    private String type;

    @NotBlank(message = "게시글 제목은 필수 입력값입니다.")
    private String title;

    @NotBlank(message = "게시글 내용을 입력해주세요.")
    private String content;

    private Long userId;
    
}
