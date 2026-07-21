package com.daenggo.backend.board.dto;

import com.daenggo.backend.board.entity.Board;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class BoardResponse {
    private Long id;
    private String type;
    private String title;
    private String content;
    private Integer viewCount;
    private LocalDateTime createdAt;
    private String nickname;
    private List<String> imageUrls;


    public static BoardResponse from(Board board) {
        return from(board, List.of());
    }

    public static BoardResponse from(Board board, List<String> imageUrls) {
        return BoardResponse.builder()
                .id(board.getId())
                .type(board.getType())
                .title(board.getTitle())
                .content(board.getContent())
                .viewCount(board.getViewCount())
                .createdAt(board.getCreatedAt())
                .nickname(board.getUser().getNickname())
                .imageUrls(imageUrls == null ? List.of() : List.copyOf(imageUrls))
                .build();
    }
}
