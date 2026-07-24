package com.daenggo.backend.place.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 출입 규정 변경 이력 (한 번의 변경 단위)
 *
 * 같은 시각·사유로 발생한 항목별 변경을 하나로 묶어 응답한다.
 */
public record PolicyChangeResponse(
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime changedAt,
        String reason,
        List<PolicyHistoryResponse> changes
) {}