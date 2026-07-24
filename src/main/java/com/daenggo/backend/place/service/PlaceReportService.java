package com.daenggo.backend.place.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.daenggo.backend.place.dto.PlaceReportRequest;
import com.daenggo.backend.place.dto.PlaceReportResponse;
import com.daenggo.backend.place.entity.Place;
import com.daenggo.backend.place.entity.PlaceReport;
import com.daenggo.backend.place.repository.PlaceReportRepository;
import com.daenggo.backend.place.repository.PlaceRepository;
import com.daenggo.backend.user.entity.User;
import com.daenggo.backend.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlaceReportService {

    private final PlaceReportRepository reportRepository;
    private final PlaceRepository placeRepository;
    private final UserRepository userRepository;
    private final PolicyHistoryService policyHistoryService;

    /** 장소 정보 오류 신고 등록 (상태는 PENDING으로 자동 설정) */
    @Transactional
    public Long createReport(Long userId, Long placeId, PlaceReportRequest request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));
        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new IllegalArgumentException("장소를 찾을 수 없습니다."));

        PlaceReport report = PlaceReport.builder()
                .user(user)
                .place(place)
                .reportType(request.reportType())
                .content(request.content())
                .createdAt(LocalDateTime.now())
                .build();

        return reportRepository.save(report).getReportId();
    }

    /** 내가 작성한 신고 목록 조회 (최신순) */
    public List<PlaceReportResponse> getMyReports(Long userId) {
        return reportRepository.findByUser_IdOrderByCreatedAtDesc(userId)
                .stream()
                .map(PlaceReportResponse::from)
                .toList();
    }
    
    /**
     * 신고 내용 수정
     *
     * 본인이 작성한 신고이면서 처리 대기(PENDING) 상태일 때만 수정할 수 있다.
     */
    @Transactional
    public void updateReport(Long userId, Long reportId, PlaceReportRequest request) {

        PlaceReport report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("신고를 찾을 수 없습니다."));

        validateOwner(report, userId);

        if (!report.isPending()) {
            throw new IllegalStateException("처리가 완료된 신고는 수정할 수 없습니다.");
        }

        report.update(request.reportType(), request.content());
    }

    /**
     * 신고 취소
     *
     * 본인이 작성한 신고이면서 처리 대기(PENDING) 상태일 때만 취소할 수 있다.
     */
    @Transactional
    public void cancelReport(Long userId, Long reportId) {

        PlaceReport report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("신고를 찾을 수 없습니다."));

        validateOwner(report, userId);

        if (!report.isPending()) {
            throw new IllegalStateException("처리가 완료된 신고는 취소할 수 없습니다.");
        }

        reportRepository.delete(report);
    }

    /** 신고 작성자 본인인지 확인 */
    private void validateOwner(PlaceReport report, Long userId) {
        if (!report.getUser().getId().equals(userId)) {
            throw new IllegalStateException("본인이 작성한 신고만 처리할 수 있습니다.");
        }
    }
    
    /**
     * 신고를 승인하고 출입 조건을 수정한다.
     *
     * 조건 변경 시 변경 이력이 함께 기록된다.
     * 관리자 기능이므로 추후 권한 검증이 필요하다.
     */
    @Transactional
    public int approveReport(Long reportId,
                             String indoorStatus,
                             String leashRequired,
                             String muzzleRequired,
                             String dangerousAllowed,
                             BigDecimal maxWeight,
                             String allowedSize) {

        PlaceReport report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("신고를 찾을 수 없습니다."));

        if (!report.isPending()) {
            throw new IllegalStateException("이미 처리된 신고입니다.");
        }

        int recorded = policyHistoryService.updateWithHistory(
                report.getPlace().getPlaceId(),
                indoorStatus, leashRequired, muzzleRequired,
                dangerousAllowed, maxWeight, allowedSize,
                "USER_REPORT");

        report.approve();
        return recorded;
    }

    /**
     * 신고를 반려한다.
     *
     * 관리자 기능이므로 추후 권한 검증이 필요하다.
     */
    @Transactional
    public void rejectReport(Long reportId) {
        PlaceReport report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("신고를 찾을 수 없습니다."));

        if (!report.isPending()) {
            throw new IllegalStateException("이미 처리된 신고입니다.");
        }
        report.reject();
    }
}