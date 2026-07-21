package com.daenggo.backend.walk.dto;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import lombok.Getter;

public class WalkRequestDto {

    @Getter
    public static class WalkCompleteRequest {      // 산책 완료 시

        private String title;

        private String memo;

        private List<Long> petIds;

        private BigDecimal distanceM;

        private String status;
    }

    @Getter
    public static class RoutePointRequest {        // GPS 좌표 저장

        private Long sequenceNo;

        private BigDecimal latitude;

        private BigDecimal longitude;

        private BigDecimal altitudeM;
    }

    @Getter
    public static class WalkRouteBatchRequest {

        private List<RoutePointRequest> trackPoints;
    }

    @Getter
    public static class WalkUpdateRequest {        // 산책 수정

        private String title;

        private String memo;

        private String status;
    }

    @Getter
    public static class WalkPhotoUploadRequest {   // 사진 등록

        private MultipartFile image;

        private String caption;

        private BigDecimal latitude;

        private BigDecimal longitude;

        private Long placeId;
    }
}