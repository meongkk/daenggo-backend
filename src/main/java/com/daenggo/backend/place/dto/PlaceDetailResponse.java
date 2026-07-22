package com.daenggo.backend.place.dto;

import com.daenggo.backend.place.entity.Place;
import com.daenggo.backend.place.entity.PlaceCondition;
import java.math.BigDecimal;

/** 장소 상세 조회 응답 (반려동물 출입 조건 포함) */
public record PlaceDetailResponse(
        Long placeId,
        String title,
        String category,
        String address,
        String tel,
        String openTime,
        String restDate,
        String parking,
        BigDecimal latitude,
        BigDecimal longitude,
        String thumbnail,
        ConditionResponse condition
) {

    /**
     * 반려동물 출입 조건
     *
     * 관광공사 API에 정보가 등록되지 않은 장소는 null로 응답된다.
     */
    public record ConditionResponse(
            String indoorStatus,
            String leashRequired,
            String muzzleRequired,
            String dangerousAllowed,
            String allowedSize,
            BigDecimal maxWeight,
            String amenities,
            String rawText
    ) {
        /** PlaceCondition 엔티티를 응답 형식으로 변환 (없으면 null) */
        public static ConditionResponse from(PlaceCondition c) {
            if (c == null) {
                return null;
            }
            return new ConditionResponse(
                    c.getIndoorStatus(),
                    c.getLeashRequired(),
                    c.getMuzzleRequired(),
                    c.getDangerousAllowed(),
                    c.getAllowedSize(),
                    c.getMaxWeight(),
                    c.getAmenities(),
                    c.getRawText());
        }
    }

    /** Place와 PlaceCondition을 합쳐 상세 응답을 생성 */
    public static PlaceDetailResponse from(Place place, PlaceCondition condition) {
        return new PlaceDetailResponse(
                place.getPlaceId(),
                place.getTitle(),
                place.getCategory(),
                place.getAddress(),
                place.getTel(),
                place.getOpenTime(),
                place.getRestDate(),
                place.getParking(),
                place.getLatitude(),
                place.getLongitude(),
                place.getThumbnail(),
                ConditionResponse.from(condition));
    }
}