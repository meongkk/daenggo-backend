package com.daenggo.backend.walk.service;

import java.util.List;

import com.daenggo.backend.walk.dto.WalkResponseDto.WalkCalendarResponse;
import com.daenggo.backend.walk.dto.WalkResponseDto.WalkCompleteRequest;
import com.daenggo.backend.walk.dto.WalkResponseDto.WalkCompleteResponse;
import com.daenggo.backend.walk.dto.WalkResponseDto.WalkDetailResponse;
import com.daenggo.backend.walk.dto.WalkResponseDto.WalkListResponse;
import com.daenggo.backend.walk.dto.WalkResponseDto.WalkPhotoResponse;
import com.daenggo.backend.walk.dto.WalkResponseDto.WalkPhotoUploadRequest;
import com.daenggo.backend.walk.dto.WalkResponseDto.WalkRouteBatchRequest;
import com.daenggo.backend.walk.dto.WalkResponseDto.WalkRouteResponse;
import com.daenggo.backend.walk.dto.WalkResponseDto.WalkUpdateRequest;
import com.daenggo.backend.walk.dto.WalkResponseDto.WalkeStartResponse;

public interface WalkService {

    /**
     * 산책 시작
     */
    WalkeStartResponse startWalk(Long userId);

    /**
     * GPS 좌표 일괄 저장
     */
    void saveTrackPoints(Long walkId, WalkRouteBatchRequest request);

    /**
     * 산책 종료
     */
    WalkCompleteResponse completeWalk(
            Long userId,
            Long walkId,
            WalkCompleteRequest request
    );

    /**
     * 산책 목록 조회
     */
    List<WalkListResponse> getWalkList(Long userId);

    /**
     * 산책 상세 조회
     */
    WalkDetailResponse getWalk(Long userId, Long walkId);

    /**
     * 산책 경로 조회
     */
    WalkRouteResponse getWalkRoute(Long userId, Long walkId);

    /**
     * 산책 기록 수정
     */
    WalkDetailResponse updateWalk(
            Long userId,
            Long walkId,
            WalkUpdateRequest request
    );

    /**
     * 산책 기록 삭제
     */
    void deleteWalk(Long userId, Long walkId);

    /**
     * 월별 캘린더 조회
     */
    WalkCalendarResponse getCalendar(
            Long userId,
            int year,
            int month
    );

    /**
     * 산책 사진 등록
     */
    WalkPhotoResponse uploadPhoto(
            Long userId,
            Long walkId,
            WalkPhotoUploadRequest request
    );

    /**
     * 산책 사진 삭제
     */
    void deletePhoto(
            Long userId,
            Long walkId,
            Long photoId
    );
}