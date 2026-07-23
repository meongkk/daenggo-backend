package com.daenggo.backend.place.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/**
 * 관광공사 API 응답 매핑용 DTO
 *
 * API가 반환하는 JSON 구조(response → body → items → item)를 그대로 따름.
 * 필드명이 소문자인 것은 JSON 키와 일치시켜 자동 매핑되도록 하기 위함.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record TourApiResponse(Response response) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Response(Header header, Body body) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Header(String resultCode, String resultMsg) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Body(Items items, Integer numOfRows, Integer pageNo, Integer totalCount) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Items(List<Item> item) {}

    /** 장소 목록 항목 (locationBasedList2) */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Item(
            String contentid,
            String contenttypeid,
            String title,
            String addr1,
            String tel,
            String mapx,          // 경도
            String mapy,          // 위도
            String firstimage,
            String modifiedtime,
            String dist
    ) {}
    
    /** 반려동물 동반 상세 항목 (detailPetTour2) */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PetItem(
            String contentid,
            String acmpyTypeCd,        // 동반 유형
            String acmpyPsblCpam,      // 동반 가능 동물
            String acmpyNeedMtr,       // 동반 시 필요사항
            String etcAcmpyInfo,       // 기타 정보 (맹견·입마개 정보)
            String relaPosesFclty,     // 구비 시설
            String relaAcdntRiskMtr    // 사고 위험 사항
    ) {}
}