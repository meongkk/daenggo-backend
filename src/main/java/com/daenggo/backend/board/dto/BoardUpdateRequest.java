package com.daenggo.backend.board.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 게시글 수정 API가 프론트엔드에서 전달받는 데이터.
 */
@Getter
@NoArgsConstructor
public class BoardUpdateRequest {

    /** 수정을 요청한 사용자 식별자. */
    @NotNull(message = "수정을 요청한 사용자 ID가 필요합니다.")
    @Positive(message = "사용자 ID는 양수여야 합니다.")
    private Long userId;

    /** 변경할 게시글 제목. */
    @NotBlank(message = "수정할 제목을 입력해주세요.")
    @Size(max = 255, message = "게시글 제목은 255자 이하여야 합니다.")
    private String title;

    /** 변경할 게시글 내용. */
    @NotBlank(message = "수정할 내용을 입력해주세요.")
    private String content;

    /** 수정 후 게시글에 남길 이미지 URL 목록. 값이 없으면 기존 이미지를 유지한다. */
    @Size(max = 5, message = "이미지는 최대 5장까지 등록할 수 있습니다.")
    private List<@NotBlank(message = "이미지 주소가 비어 있습니다.") String> imageUrls;
}
