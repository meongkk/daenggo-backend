package com.daenggo.backend.place.converter;

import com.daenggo.backend.place.api.TourApiResponse;
import com.daenggo.backend.place.entity.Place;
import com.daenggo.backend.place.entity.PlaceCondition;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 관광공사 API의 자유 텍스트 출입 조건을 구조화된 값으로 파싱
 *
 * 파싱이 완전하지 않을 수 있으므로 rawText에 원문을 함께 보존.
 * 정보가 없는 항목은 false(불가)가 아닌 UNKNOWN(정보 없음)으로 처리.
 */
@Component
public class PlaceConditionConverter {

    /** "9kg 이하", "10 kg 미만" 등에서 숫자 추출 */
    private static final Pattern WEIGHT_PATTERN =
            Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*kg");

    /** API 응답을 PlaceCondition 엔티티로 변환 */
    public PlaceCondition toEntity(Place place, TourApiResponse.PetItem item) {

        String typeCd   = safe(item.acmpyTypeCd());
        String psblCpam = safe(item.acmpyPsblCpam());
        String needMtr  = safe(item.acmpyNeedMtr());
        String etcInfo  = safe(item.etcAcmpyInfo());

        return PlaceCondition.builder()
                .place(place)
                .indoorStatus(parseIndoorStatus(typeCd, etcInfo))
                .leashRequired(parseLeash(needMtr))
                .muzzleRequired(parseMuzzle(etcInfo))
                .dangerousAllowed(parseDangerous(etcInfo))
                .maxWeight(parseMaxWeight(psblCpam))
                .amenities(emptyToNull(item.relaPosesFclty()))
                .rawText(buildRawText(item))
                .build();
    }

    /**
     * 실내 출입 가능 여부 판정
     *
     * "안내견만 이용가능"은 일반 반려동물 불가를 의미하므로 DENIED로 처리.
     */
    private String parseIndoorStatus(String typeCd, String etcInfo) {
        if (etcInfo.contains("안내견만")) {
            return "DENIED";                 // 일반 반려동물 불가
        }
        if (typeCd.contains("전구역")) {
            return "ALLOWED";
        }
        if (typeCd.contains("일부구역")) {
            return "SEPARATE";
        }
        if (typeCd.contains("실외") || typeCd.contains("야외")) {
            return "OUTDOOR_ONLY";
        }
        return "UNKNOWN";
    }

    /** 목줄 필수 여부 판정 (목줄 또는 매너벨트 언급 시 REQUIRED) */
    private String parseLeash(String needMtr) {
        if (needMtr.contains("목줄") || needMtr.contains("매너벨트")) {
            return "REQUIRED";
        }
        return "UNKNOWN";
    }

    /** 입마개 필수 여부 판정 */
    private String parseMuzzle(String etcInfo) {
        if (etcInfo.contains("입마개")) {
            return "REQUIRED";
        }
        return "UNKNOWN";
    }

    /** 맹견 출입 가능 여부 판정 (입마개 조건이 있으면 CONDITIONAL) */
    private String parseDangerous(String etcInfo) {
        if (etcInfo.contains("맹견")) {
            if (etcInfo.contains("입마개")) {
                return "CONDITIONAL";        // 입마개 착용 시 가능
            }
            if (etcInfo.contains("불가") || etcInfo.contains("금지")) {
                return "DENIED";
            }
        }
        return "UNKNOWN";
    }

    /** 무게 제한 추출 (제한이 없으면 null) */
    private BigDecimal parseMaxWeight(String psblCpam) {
        Matcher matcher = WEIGHT_PATTERN.matcher(psblCpam);
        return matcher.find() ? new BigDecimal(matcher.group(1)) : null;
    }

    /** 파싱 오류 대비용 원문 보존 (항목별 라벨을 붙여 저장) */
    private String buildRawText(TourApiResponse.PetItem item) {
        StringBuilder sb = new StringBuilder();
        append(sb, "동반유형", item.acmpyTypeCd());
        append(sb, "동반가능동물", item.acmpyPsblCpam());
        append(sb, "필요사항", item.acmpyNeedMtr());
        append(sb, "기타정보", item.etcAcmpyInfo());
        append(sb, "구비시설", item.relaPosesFclty());
        append(sb, "주의사항", item.relaAcdntRiskMtr());
        String result = sb.toString().trim();
        return result.isEmpty() ? null : result;
    }

    private void append(StringBuilder sb, String label, String value) {
        if (value != null && !value.isBlank()) {
            sb.append("[").append(label).append("] ").append(value).append("\n");
        }
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String emptyToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }
}