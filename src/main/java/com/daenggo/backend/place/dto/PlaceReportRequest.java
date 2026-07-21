package com.daenggo.backend.place.dto;

public record PlaceReportRequest(
        String reportType,   // ENTRY_DENIED / CONDITION_DIFF / CLOSED / ETC
        String content
) {}