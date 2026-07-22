package com.daenggo.backend.place.converter;

import com.daenggo.backend.place.api.TourApiResponse;
import com.daenggo.backend.place.entity.Place;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** 관광공사 API 응답을 Place 엔티티로 변환 */
@Slf4j
@Component
public class PlaceConverter {

    private static final DateTimeFormatter API_DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    /** API 목록 항목을 Place 엔티티로 변환 */
    public Place toEntity(TourApiResponse.Item item) {
        return Place.builder()
                .contentId(item.contentid())
                .title(item.title())
                .category(toCategory(item.contenttypeid()))
                .address(emptyToNull(item.addr1()))
                .tel(emptyToNull(item.tel()))
                .latitude(toDecimal(item.mapy()))      // y = 위도
                .longitude(toDecimal(item.mapx()))     // x = 경도
                .thumbnail(emptyToNull(item.firstimage()))
                .apiModifiedAt(toDateTime(item.modifiedtime()))
                .createdAt(LocalDateTime.now())
                .build();
    }

    /** 관광공사 contentTypeId를 우리 카테고리 코드로 변환 */
    private String toCategory(String contentTypeId) {
        if (contentTypeId == null) return "ETC";
        return switch (contentTypeId) {
            case "12" -> "TOURIST";      // 관광지
            case "14" -> "CULTURE";      // 문화시설
            case "15" -> "FESTIVAL";     // 축제공연행사
            case "28" -> "LEISURE";      // 레포츠
            case "32" -> "LODGING";      // 숙박
            case "38" -> "SHOPPING";     // 쇼핑
            case "39" -> "RESTAURANT";   // 음식점
            default   -> "ETC";
        };
    }

    /** 문자열 좌표를 BigDecimal로 변환 (실패 시 null) */
    private BigDecimal toDecimal(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException e) {
            log.warn("좌표 변환 실패: {}", value);
            return null;
        }
    }

    /** "20251023143000" 형식 문자열을 LocalDateTime으로 변환 */
    private LocalDateTime toDateTime(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return LocalDateTime.parse(value, API_DATE_FORMAT);
        } catch (Exception e) {
            log.warn("날짜 변환 실패: {}", value);
            return null;
        }
    }

    /** 빈 문자열을 null로 변환 (API가 ""를 자주 반환함) */
    private String emptyToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }
}