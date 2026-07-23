package com.daenggo.backend.place.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 장소 출입 규정 변경 이력
 *
 * 출입 조건이 변경될 때 항목별로 변경 전후 값을 기록한다.
 * 관광공사 API 갱신(API_SYNC)과 사용자 신고 반영(USER_REPORT) 두 경로로 기록된다.
 */
@Entity
@Table(name = "policy_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PolicyHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long historyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id", nullable = false)
    private Place place;

    /** 변경된 항목명 (indoorStatus, maxWeight 등) */
    @Column(length = 30, nullable = false)
    private String field;

    /** 변경 전 값 (없었으면 null) */
    @Column(length = 100)
    private String beforeValue;

    /** 변경 후 값 */
    @Column(length = 100)
    private String afterValue;

    /** 변경 사유 (API_SYNC / USER_REPORT / ADMIN) */
    @Column(length = 20, nullable = false)
    private String reason;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}