package com.daenggo.backend.board.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "sitter_board")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SitterBoard {

    @Id
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "post_id")
    private Board board;

    @Column(name = "care_location", length = 255)
    private String careLocation; // 돌봄 위치

    @Column(name = "sitter_status", nullable = false, length = 20)
    private String sisterStatus; // 매칭 상태: 구합니다 , 원합니다

    @Builder
    public SitterBoard(Board board, String careLocation) {
        this.board = board;
        this.careLocation = careLocation;
    }
}