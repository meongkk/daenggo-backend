package com.daenggo.backend.walk.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.Column;
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

    }

    @Getter
    @AllArgsConstructor
    public static class WalkRouteBatchResponse {

        private int savedCount;
    }

    @Getter
    @Builder
    public static class WalkDetailResponse {        // 산책 상세 조회

        private Long walkRecordId;

        private String title;

        private String memo;

        private LocalDateTime startedAt;

        private BigDecimal distanceM;

        private Integer durationSec;

        private Integer avgPaceSec;

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

        private BigDecimal latitude;

        private BigDecimal longitude;

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

        private BigDecimal latitude;

        private BigDecimal longitude;

        private LocalDateTime takenAt;
    }
}