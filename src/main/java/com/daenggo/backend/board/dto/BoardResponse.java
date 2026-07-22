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
    private Long commentCount;


    public static BoardResponse from(Board board) {
        return from(board, List.of(), 0L);
    }

    public static BoardResponse from(Board board, List<String> imageUrls) {
        return from(board, imageUrls, 0L);
    }

    public static BoardResponse from(
            Board board,
            List<String> imageUrls,
            long commentCount
    ) {
        return BoardResponse.builder()
                .id(board.getId())
                .type(board.getType())
                .title(board.getTitle())
                .content(board.getContent())
                .viewCount(board.getViewCount())
                .createdAt(board.getCreatedAt())
                .nickname(board.getUser().getNickname())
                .imageUrls(imageUrls == null ? List.of() : List.copyOf(imageUrls))
                .commentCount(commentCount)
                .build();
    }
}
