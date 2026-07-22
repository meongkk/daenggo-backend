package com.daenggo.backend.place.dto;

import java.time.LocalDateTime;

import com.daenggo.backend.place.entity.PlaceReport;
import com.fasterxml.jackson.annotation.JsonFormat;

public record PlaceReportResponse(
        Long reportId,
        Long placeId,
        String placeTitle,
        String reportType,
        String content,
        String status,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime createdAt
) {
    public static PlaceReportResponse from(PlaceReport report) {
        return new PlaceReportResponse(
                report.getReportId(),
                report.getPlace().getPlaceId(),
                report.getPlace().getTitle(),
                report.getReportType(),
                report.getContent(),
                report.getStatus(),
                report.getCreatedAt()
        );
    }
}