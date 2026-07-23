package com.daenggo.backend.board.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;


/**
 * 게시글 삭제를 요청한 사용자의 정보를 전달한다.
 *
 * @param userId 삭제를 요청한 사용자 식별자
 */
public record BoardDeleteRequest(
        @NotNull (message = "삭제를 요청한 사용자 ID가 필요합니다.")
        @Positive (message = "사용자 ID는 양수여야 합니다.")
        Long userId
) {
}