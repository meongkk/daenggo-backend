package com.daenggo.backend.place.dto;

/** 지역별 장소 개수 */
public record RegionCountResponse(String region, long count) {}