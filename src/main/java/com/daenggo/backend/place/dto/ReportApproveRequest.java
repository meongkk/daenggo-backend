package com.daenggo.backend.place.dto;

import java.math.BigDecimal;

/**
 * 신고 승인 요청 (수정할 출입 조건)
 *
 * 관리자가 신고 내용을 확인한 뒤 반영할 값을 전달한다.
 */
public record ReportApproveRequest(
        String indoorStatus,
        String leashRequired,
        String muzzleRequired,
        String dangerousAllowed,
        BigDecimal maxWeight,
        String allowedSize
) {}