package com.daenggo.backend.place.dto;

import com.daenggo.backend.place.entity.PolicyHistory;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * 출입 규정 변경 이력 응답
 *
 * field는 변경된 항목의 필드명이고, fieldLabel은 화면 표시용 한글 이름이다.
 * 프론트에서 별도 매핑 없이 라벨을 그대로 사용할 수 있도록 함께 내려준다.
 */
public record PolicyHistoryResponse(
        Long historyId,
        String field,
        String fieldLabel,
        String beforeValue,
        String afterValue,
        String reason,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime createdAt
) {

	/** PolicyHistory 엔티티를 응답 형식으로 변환 */
    public static PolicyHistoryResponse from(PolicyHistory history) {
        return new PolicyHistoryResponse(
                history.getHistoryId(),
                history.getField(),
                toLabel(history.getField()),
                history.getBeforeValue(),
                history.getAfterValue(),
                history.getReason(),
                history.getCreatedAt());
    }

    /** 필드명을 화면 표시용 한글 라벨로 변환 */
    private static String toLabel(String field) {
        return switch (field) {
            case "indoorStatus"     -> "실내 출입";
            case "leashRequired"    -> "목줄";
            case "muzzleRequired"   -> "입마개";
            case "dangerousAllowed" -> "맹견 출입";
            case "maxWeight"        -> "무게 제한";
            case "allowedSize"      -> "크기 제한";
            default                 -> field;
        };
    }
}