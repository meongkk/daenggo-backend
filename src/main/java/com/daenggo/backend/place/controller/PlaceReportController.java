package com.daenggo.backend.place.controller;

import com.daenggo.backend.place.dto.PlaceReportRequest;
import com.daenggo.backend.place.dto.PlaceReportResponse;
import com.daenggo.backend.place.service.PlaceReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class PlaceReportController {

    private final PlaceReportService reportService;

    /** 장소 정보 오류 신고 등록 */
    @PostMapping("/api/places/{placeId}/reports")
    public Long createReport(@PathVariable Long placeId,
                             @RequestParam Long userId,        // TODO: 인증에서 추출
                             @RequestBody PlaceReportRequest request) {
        return reportService.createReport(userId, placeId, request);
    }

    /** 내 신고 목록 조회 */
    @GetMapping("/api/users/me/place-reports")
    public List<PlaceReportResponse> getMyReports(@RequestParam Long userId) {  // TODO
        return reportService.getMyReports(userId);
    }
    
    /** 신고 내용 수정 (대기 상태만 가능) */
    @PatchMapping("/api/place-reports/{reportId}")
    public void updateReport(@PathVariable Long reportId,
                             @RequestParam Long userId,        // TODO: 인증에서 추출
                             @RequestBody PlaceReportRequest request) {
        reportService.updateReport(userId, reportId, request);
    }

    /** 신고 취소 (대기 상태만 가능) */
    @DeleteMapping("/api/place-reports/{reportId}")
    public void cancelReport(@PathVariable Long reportId,
                             @RequestParam Long userId) {      // TODO
        reportService.cancelReport(userId, reportId);
    }
}