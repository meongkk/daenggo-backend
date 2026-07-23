package com.daenggo.backend.place.service;

import com.daenggo.backend.place.dto.PlaceReportRequest;
import com.daenggo.backend.place.dto.PlaceReportResponse;
import com.daenggo.backend.place.entity.Place;
import com.daenggo.backend.place.entity.PlaceReport;
import com.daenggo.backend.place.repository.PlaceReportRepository;
import com.daenggo.backend.place.repository.PlaceRepository;
import com.daenggo.backend.user.entity.User;
import com.daenggo.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlaceReportService {

    private final PlaceReportRepository reportRepository;
    private final PlaceRepository placeRepository;
    private final UserRepository userRepository;

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
}