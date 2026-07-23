package com.daenggo.backend.place.converter;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

/**
 * 관광공사 소개 정보(detailIntro2)를 우리 형식으로 변환
 *
 * 응답 필드명이 카테고리(contentTypeId)마다 다르므로 분기 처리한다.
 */
@Component
public class PlaceIntroConverter {

    /** 우리 카테고리 코드를 관광공사 contentTypeId로 역변환 */
    public String toContentTypeId(String category) {
        return switch (category) {
            case "TOURIST"    -> "12";
            case "CULTURE"    -> "14";
            case "FESTIVAL"   -> "15";
            case "LEISURE"    -> "28";
            case "LODGING"    -> "32";
            case "SHOPPING"   -> "38";
            case "RESTAURANT" -> "39";
            default           -> null;
        };
    }

    /** 전화번호 (카테고리별 필드명이 다름) */
    public String extractTel(JsonNode item, String category) {
        return switch (category) {
            case "LODGING"    -> text(item, "infocenterlodging");
            case "RESTAURANT" -> text(item, "infocenterfood");
            case "SHOPPING"   -> text(item, "infocentershopping");
            case "LEISURE"    -> text(item, "infocenterleports");
            case "CULTURE"    -> text(item, "infocenterculture");
            default           -> text(item, "infocenter");
        };
    }

    /** 운영시간 (숙박은 체크인·아웃으로 조합) */
    public String extractOpenTime(JsonNode item, String category) {
        if ("LODGING".equals(category)) {
            String in  = text(item, "checkintime");
            String out = text(item, "checkouttime");
            if (in == null && out == null) return null;
            return "체크인 " + (in != null ? in : "-")
                 + " / 체크아웃 " + (out != null ? out : "-");
        }
        return switch (category) {
            case "RESTAURANT" -> text(item, "opentimefood");
            case "SHOPPING"   -> text(item, "opentime");
            case "CULTURE"    -> text(item, "usetimeculture");
            case "LEISURE"    -> text(item, "usetimeleports");
            default           -> text(item, "usetime");
        };
    }

    /** 휴무일 */
    public String extractRestDate(JsonNode item, String category) {
        return switch (category) {
            case "RESTAURANT" -> text(item, "restdatefood");
            case "SHOPPING"   -> text(item, "restdateshopping");
            case "CULTURE"    -> text(item, "restdateculture");
            case "LEISURE"    -> text(item, "restdateleports");
            default           -> text(item, "restdate");
        };
    }

    /** 주차 가능 여부 */
    public String extractParking(JsonNode item, String category) {
        return switch (category) {
            case "LODGING"    -> text(item, "parkinglodging");
            case "RESTAURANT" -> text(item, "parkingfood");
            case "SHOPPING"   -> text(item, "parkingshopping");
            case "CULTURE"    -> text(item, "parkingculture");
            case "LEISURE"    -> text(item, "parkingleports");
            default           -> text(item, "parking");
        };
    }

    /** JsonNode에서 문자열 추출 (HTML 태그 제거, 없거나 비었으면 null) */
    private String text(JsonNode node, String field) {
        JsonNode value = node.path(field);
        if (value.isMissingNode() || value.asText().isBlank()) return null;

        String result = value.asText()
                .replaceAll("(?i)<[^>]+>", " ")   // 모든 HTML 태그 제거 (대소문자 무시)
                .replaceAll("\\s+", " ")           // 연속 공백 정리
                .trim();

        return result.isBlank() ? null : result;
    }
}