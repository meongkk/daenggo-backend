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
 * 표현이 정형화되어 있지 않아 완전한 파싱이 불가능하다.
 * 잘못 "가능"으로 판정하면 사용자가 헛걸음하므로,
 * 애매한 경우 UNKNOWN(정보 없음)으로 두는 것을 원칙으로 한다.
 * 원문은 rawText에 보존하여 상세 화면에서 함께 노출한다.
 */
@Component
public class PlaceConditionConverter {

    /** "9kg 이하", "10KG 미만" 등에서 숫자 추출 (대소문자 무시) */
    private static final Pattern WEIGHT_PATTERN =
            Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*kg", Pattern.CASE_INSENSITIVE);

    public PlaceCondition toEntity(Place place, TourApiResponse.PetItem item) {

        String typeCd   = safe(item.acmpyTypeCd());
        String psblCpam = safe(item.acmpyPsblCpam());
        String needMtr  = safe(item.acmpyNeedMtr());
        String etcInfo  = safe(item.etcAcmpyInfo());

        BigDecimal maxWeight = parseMaxWeight(psblCpam, etcInfo);

        return PlaceCondition.builder()
                .place(place)
                .indoorStatus(parseIndoorStatus(typeCd, psblCpam, etcInfo))
                .leashRequired(parseLeash(needMtr))
                .muzzleRequired(parseMuzzle(psblCpam, etcInfo))
                .dangerousAllowed(parseDangerous(psblCpam, etcInfo))
                .maxWeight(maxWeight)
                // 무게가 명시된 경우 크기는 저장하지 않는다 (무게가 더 정확한 기준)
                .allowedSize(maxWeight != null ? null : parseAllowedSize(psblCpam, etcInfo))
                .amenities(emptyToNull(item.relaPosesFclty()))
                .rawText(buildRawText(item))
                .build();
    }

    /**
     * 실내 출입 가능 여부
     *
     * 안내견만 허용하는 장소는 일반 반려동물 불가로 판정한다.
     * 단, "안내견은 모든 시설 가능" 처럼 일반 동반 조건과 함께 언급된 경우는
     * 안내견에 대한 추가 안내이므로 제외한다.
     */
    private String parseIndoorStatus(String typeCd, String psblCpam, String etcInfo) {
        String cpam = compact(psblCpam);
        String all  = compact(psblCpam + etcInfo);

        if (isGuideDogOnly(cpam, all)) {
            return "DENIED";
        }
        if (typeCd.contains("전구역")) return "ALLOWED";
        if (typeCd.contains("일부구역")) return "SEPARATE";
        if (typeCd.contains("실외") || typeCd.contains("야외")) return "OUTDOOR_ONLY";
        return "UNKNOWN";
    }
    
    /**
     * 안내견 전용 장소인지 판정
     *
     * 동반가능동물 항목이 안내견 언급만으로 구성된 경우를 전용으로 본다.
     * 무게·견종 등 일반 동반 조건이 함께 있으면 전용이 아니다.
     */
    private boolean isGuideDogOnly(String cpam, String all) {
        if (!all.contains("안내견")) {
            return false;
        }
        // 명시적 전용 표현
        if (all.contains("안내견만")) {
            return true;
        }
        // "안내견 이외 출입 금지" 형태
        if (all.matches(".*안내견.{0,10}(이외|외).{0,15}(금지|불가|제외).*")) {
            return true;
        }
        // 동반가능동물에 안내견 외 다른 조건이 없으면 전용으로 판단
        if (cpam.contains("안내견")) {
            String rest = cpam.replaceAll("시각|청각|맹인|장애인|장애우|안내견|보조견|도우미견", "");
            return rest.length() < 5;
        }
        return false;
    }

    /** 목줄 필수 여부 */
    private String parseLeash(String needMtr) {
        String s = compact(needMtr);
        if (s.contains("목줄") || s.contains("매너벨트") || s.contains("이동장")
                || s.contains("켄넬")) {
            return "REQUIRED";
        }
        return "UNKNOWN";
    }

    /** 입마개 필수 여부 */
    private String parseMuzzle(String psblCpam, String etcInfo) {
        String all = compact(psblCpam + etcInfo);
        if (all.contains("입마개불가") || all.contains("입마개없이")) {
            return "UNKNOWN";
        }
        return all.contains("입마개") ? "REQUIRED" : "UNKNOWN";
    }

    /**
     * 맹견 출입 가능 여부
     *
     * "맹견 제외", "맹견 및 대형견 제외", "맹견 입장 불가" 등
     * 맹견과 부정어 사이에 다른 표현이 끼는 경우가 많아 정규식으로 처리한다.
     */
    private String parseDangerous(String psblCpam, String etcInfo) {
        String all = compact(psblCpam + etcInfo);

        // 맹견 뒤 20자 이내에 부정 표현이 오면 불가로 판정
        if (all.matches(".*맹견.{0,20}(제외|불가|금지|X|x|안됨|없음).*")) {
            return "DENIED";
        }
        if (all.contains("맹견") && all.contains("입마개")) {
            return "CONDITIONAL";
        }
        return "UNKNOWN";
    }

    /**
     * 무게 제한 추출
     *
     * 여러 숫자가 나오면 가장 작은 값을 채택한다.
     * 예) "12kg 미만의 소형견 ... 소형견(10kg미만)까지 가능" → 10
     * 잘못 통과시키는 것보다 엄격하게 거르는 편이 안전하다.
     */
    private BigDecimal parseMaxWeight(String psblCpam, String etcInfo) {
        Matcher m = WEIGHT_PATTERN.matcher(psblCpam + " " + etcInfo);
        BigDecimal min = null;
        while (m.find()) {
            BigDecimal value = new BigDecimal(m.group(1));
            if (min == null || value.compareTo(min) < 0) {
                min = value;
            }
        }
        return min;
    }

    /**
     * 허용 크기 파싱
     *
     * "대형견 불가" 같은 부정 표현이 있으면 해당 크기를 제외한다.
     * 판단이 어려우면 null(미상)로 두어 필터에서 제외되지 않게 한다.
     */
    private String parseAllowedSize(String psblCpam, String etcInfo) {
        String all = compact(psblCpam + etcInfo);

        if (all.contains("전견종") || all.contains("모든견종")) {
            return null;
        }

        // 부정 표현 우선
        boolean largeDenied = all.contains("대형견불가") || all.contains("대형견X")
                || all.contains("대형견x") || all.contains("대형견은불가")
                || all.contains("대형견입실이불가") || all.contains("대형견불가능");
        boolean mediumDenied = all.contains("중형견불가") || all.contains("중형견X");

        if (mediumDenied) return "SMALL";
        if (largeDenied)  return "MEDIUM";

        if (all.contains("대형")) return "LARGE";
        if (all.contains("중형") || all.contains("중소형")) return "MEDIUM";
        if (all.contains("소형")) return "SMALL";
        return null;
    }

    /** 파싱 오류 대비 원문 보존 (항목별 라벨 부착) */
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

    /** 공백·쉼표·가운뎃점 제거 (표기 흔들림 흡수) */
    private String compact(String value) {
        return safe(value).replaceAll("[\\s,·ㆍ]", "");
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String emptyToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }
    
    /**
     * 저장된 rawText를 기준으로 출입 조건을 다시 파싱한다.
     *
     * 파싱 규칙이 개선되었을 때 API 재호출 없이 기존 데이터를 갱신하기 위한 용도.
     */
    public void reparse(PlaceCondition condition) {
        String raw = condition.getRawText();
        if (raw == null || raw.isBlank()) return;

        String typeCd   = section(raw, "동반유형");
        String psblCpam = section(raw, "동반가능동물");
        String needMtr  = section(raw, "필요사항");
        String etcInfo  = section(raw, "기타정보");

        BigDecimal maxWeight = parseMaxWeight(psblCpam, etcInfo);

        condition.updateParsed(
                parseIndoorStatus(typeCd, psblCpam, etcInfo),
                parseLeash(needMtr),
                parseMuzzle(psblCpam, etcInfo),
                parseDangerous(psblCpam, etcInfo),
                maxWeight,
                maxWeight != null ? null : parseAllowedSize(psblCpam, etcInfo));
    }

    /** rawText에서 [라벨] 구간의 내용을 추출 */
    private String section(String raw, String label) {
        String marker = "[" + label + "]";
        int start = raw.indexOf(marker);
        if (start < 0) return "";
        start += marker.length();

        int next = raw.indexOf("\n[", start);
        return (next < 0 ? raw.substring(start) : raw.substring(start, next)).trim();
    }
}