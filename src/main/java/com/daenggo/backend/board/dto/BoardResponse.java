package com.daenggo.backend.board.dto;

import com.daenggo.backend.board.entity.Board;
import com.daenggo.backend.board.entity.MarketBoard;
import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

/**
 * 커뮤니티 게시글 API가 프론트엔드에 전달하는 응답 데이터의 모양을 정의한다.
 */
@Getter
@Builder
public class BoardResponse {
    private Long id;
    private String type;
    private String title;
    private String content;
    private Integer viewCount;

    /** UTC 시간대가 포함된 게시글 작성 시각. */
    private OffsetDateTime createdAt;
    private String nickname;
    private List<String> imageUrls;
    private Long commentCount;
    private Integer price;
    private String tradeStatus;


    public static BoardResponse from(Board board) {
        return from(board, List.of(), 0L);
    }

    public static BoardResponse from(Board board, List<String> imageUrls) {
        return from(board, imageUrls, 0L);
    }

    /**
     * 게시글 엔티티를 이미지와 댓글 개수가 포함된 API 응답 DTO로 변환한다.
     *
     * @param board 응답으로 변환할 게시글 엔티티
     * @param imageUrls 게시글에 연결된 이미지 URL 목록
     * @param commentCount 게시글에 등록된 댓글 개수
     * @return 프론트엔드에 전달할 게시글 응답 DTO
     */
    public static BoardResponse from(
            Board board,
            List<String> imageUrls,
            long commentCount
    ) {
        return from(board, imageUrls, commentCount, null);
    }

    /**
     * 일반 게시글 정보와 장터 전용 정보를 하나의 API 응답으로 합칩니다.
     * 장터 글이 아니면 marketBoard가 null이므로 price와 tradeStatus도 null입니다.
     */
    public static BoardResponse from(
            Board board,
            List<String> imageUrls,
            long commentCount,
            MarketBoard marketBoard
    ) {
        return BoardResponse.builder()
                .id(board.getId())
                .type(board.getType())
                .title(board.getTitle())
                .content(board.getContent())
                .viewCount(board.getViewCount())
                .createdAt(board.getCreatedAt() == null
                        ? null
                        : board.getCreatedAt().atOffset(ZoneOffset.UTC))
                .nickname(board.getUser().getNickname())
                .imageUrls(imageUrls == null ? List.of() : List.copyOf(imageUrls))
                .commentCount(commentCount)
                .price(marketBoard == null ? null : marketBoard.getPrice())
                .tradeStatus(marketBoard == null ? null : marketBoard.getTradeStatus())
                .build();
    }
}
