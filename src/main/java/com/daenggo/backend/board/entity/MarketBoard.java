package com.daenggo.backend.board.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "market_board")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MarketBoard {

    @Id
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "post_id")
    private Board board;

    @Column(nullable = false)
    private Integer price;

    @Column(name = "trade_status", nullable = false, length = 20)
    private String tradeStatus; // SELL(팝니다), BUY(삽니다), DONE(거래완료)

    @Builder
    public MarketBoard(Board board, Integer price, String tradeStatus) {
        this.board = board;
        this.price = price;
        this.tradeStatus = tradeStatus;
    }
}