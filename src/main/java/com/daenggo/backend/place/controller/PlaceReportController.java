package com.daenggo.backend.place.controller;

import com.daenggo.backend.place.dto.PlaceReportRequest;
import com.daenggo.backend.place.dto.PlaceReportResponse;
import com.daenggo.backend.place.dto.ReportApproveRequest;
import com.daenggo.backend.place.service.PlaceReportService;
import com.daenggo.backend.place.util.AuthUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** 장소 정보 오류 신고 API */
@RestController
@RequiredArgsConstructor
public class PlaceReportController {

    private final PlaceReportService reportService;

    /** 장소 정보 오류 신고 등록 */
    @PostMapping("/api/places/{placeId}/reports")
    public Long createReport(@PathVariable Long placeId,
                             @AuthenticationPrincipal Jwt jwt,
                             @RequestBody PlaceReportRequest request) {
        return reportService.createReport(AuthUtil.userId(jwt), placeId, request);
    }

    /** 내 신고 목록 조회 */
    @GetMapping("/api/users/me/place-reports")
    public List<PlaceReportResponse> getMyReports(@AuthenticationPrincipal Jwt jwt) {
        return reportService.getMyReports(AuthUtil.userId(jwt));
    }

    /** 신고 내용 수정 (대기 상태만 가능) */
    @PatchMapping("/api/place-reports/{reportId}")
    public void updateReport(@PathVariable Long reportId,
                             @AuthenticationPrincipal Jwt jwt,
                             @RequestBody PlaceReportRequest request) {
        reportService.updateReport(AuthUtil.userId(jwt), reportId, request);
    }

    /** 신고 취소 (대기 상태만 가능) */
    @DeleteMapping("/api/place-reports/{reportId}")
    public void cancelReport(@PathVariable Long reportId,
                             @AuthenticationPrincipal Jwt jwt) {
        reportService.cancelReport(AuthUtil.userId(jwt), reportId);
    }

    /**
     * 신고 승인 및 출입 조건 반영 (관리자용)
     *
     * TODO: 관리자 권한 검증 필요 (User에 ADMIN 역할 추가 후)
     */
    @PatchMapping("/api/place-reports/{reportId}/approve")
    public String approveReport(@PathVariable Long reportId,
                                @RequestBody ReportApproveRequest request) {
        int count = reportService.approveReport(reportId,
                request.indoorStatus(), request.leashRequired(),
                request.muzzleRequired(), request.dangerousAllowed(),
                request.maxWeight(), request.allowedSize());
        return "변경 이력 " + count + "건 기록";
    }

    /**
     * 신고 반려 (관리자용)
     *
     * TODO: 관리자 권한 검증 필요
     */
    @PatchMapping("/api/place-reports/{reportId}/reject")
    public void rejectReport(@PathVariable Long reportId) {
        reportService.rejectReport(reportId);
    }
}