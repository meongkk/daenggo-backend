package com.daenggo.backend.place.dto;

import com.daenggo.backend.place.entity.PlaceReport;
import java.time.LocalDateTime;

public record PlaceReportResponse(
        Long reportId,
        Long placeId,
        String placeTitle,
        String reportType,
        String content,
        String status,
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