package com.daenggo.backend.board.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/** 게시글 수정 요청의 제목, 내용, 이미지 목록을 담는다. */
@Getter
@NoArgsConstructor
public class BoardUpdateRequest {

    /** 변경할 게시글 제목. */
    @NotBlank(message = "수정할 제목을 입력해주세요.")
    @Size(max = 255, message = "게시글 제목은 255자 이하여야 합니다.")
    private String title;

    /** 변경할 게시글 내용. */
    @NotBlank(message = "수정할 내용을 입력해주세요.")
    private String content;

    /** 변경 후 이미지 URL 목록. null이면 기존 이미지를 유지한다. */
    @Size(max = 5, message = "이미지는 최대 5장까지 등록할 수 있습니다.")
    private List<@NotBlank(message = "이미지 주소가 비어 있습니다.") String> imageUrls;
}
