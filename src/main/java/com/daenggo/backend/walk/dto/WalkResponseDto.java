package com.daenggo.backend.walk.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

public class WalkResponseDto {

    @Getter
    @Builder
    public static class WalkStartResponse {        // 산책 시작 시

        private Long walkRecordId;

        private LocalDateTime startedAt;
    }

    @Getter
    @Builder
    public static class WalkCompleteResponse {      // 산책 완료 시

        private Long walkRecordId;

        private LocalDateTime endedAt;

        private String status;
    }

    @Getter
    @AllArgsConstructor
    public static class WalkRouteBatchResponse {

        private int savedCount;
    }

    @Getter
    @Builder
    public static class WalkListResponse {          // 산책 목록 조회

        private Long walkRecordId;

        private String title;

        private LocalDate walkDate;

        private Double distanceM;

        private Integer durationSec;

        private String thumbnailImage;

        private String status;
    }

    @Getter
    @Builder
    public static class WalkDetailResponse {        // 산책 상세 조회

        private Long walkRecordId;

        private String title;

        private String memo;

        private LocalDateTime startedAt;

        private LocalDateTime endedAt;

        private Double distanceM;

        private Integer durationSec;

        private Double avgSpeedKmh;

        private String status;

        private List<Long> petIds;
    }

    @Getter
    @Builder
    public static class WalkRouteResponse {         // 경로 조회

        private List<RoutePointResponse> routePoints;
    }

    @Getter
    @Builder
    public static class RoutePointResponse {        // 경로 조회

        private Integer sequenceNo;

        private Double latitude;

        private Double longitude;

        private Double altitudeM;
    }

    @Getter
    @Builder
    public static class WalkCalendarResponse {      // 월별 캘린더 조회

        private List<LocalDate> walkDates;
    }

    @Getter
    @Builder
    public static class WalkPhotoResponse {         // 사진 등록

        private Long photoId;

        private String imageUrl;

        private String caption;

        private Double latitude;

        private Double longitude;

        private LocalDateTime takenAt;
    }
}