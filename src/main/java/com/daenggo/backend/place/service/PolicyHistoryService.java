package com.daenggo.backend.place.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.daenggo.backend.place.dto.PolicyChangeResponse;
import com.daenggo.backend.place.dto.PolicyHistoryResponse;
import com.daenggo.backend.place.entity.Place;
import com.daenggo.backend.place.entity.PlaceCondition;
import com.daenggo.backend.place.entity.PolicyHistory;
import com.daenggo.backend.place.repository.PlaceConditionRepository;
import com.daenggo.backend.place.repository.PolicyHistoryRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


/** 장소 출입 규정 변경 이력 관리 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PolicyHistoryService {

    private final PolicyHistoryRepository historyRepository;
    private final PlaceConditionRepository conditionRepository;

    /**
     * 특정 장소의 규정 변경 이력 조회
     *
     * 같은 시각에 발생한 변경들을 하나의 그룹으로 묶어 반환한다.
     * 초 단위로 그룹핑하므로 한 번의 수정으로 발생한 항목들이 함께 묶인다.
     */
    public List<PolicyChangeResponse> getHistory(Long placeId) {

        List<PolicyHistory> histories =
                historyRepository.findByPlace_PlaceIdOrderByCreatedAtDesc(placeId);

        // 초 단위로 그룹핑 (같은 트랜잭션의 변경은 밀리초만 다름)
        Map<LocalDateTime, List<PolicyHistory>> grouped = histories.stream()
                .collect(Collectors.groupingBy(
                        h -> h.getCreatedAt().truncatedTo(ChronoUnit.SECONDS),
                        LinkedHashMap::new,
                        Collectors.toList()));

        return grouped.entrySet().stream()
                .map(entry -> new PolicyChangeResponse(
                        entry.getKey(),
                        entry.getValue().get(0).getReason(),
                        entry.getValue().stream()
                                .map(PolicyHistoryResponse::from)
                                .toList()))
                .toList();
    }

    /**
     * 출입 조건을 변경하고 변경된 항목의 이력을 기록한다.
     *
     * 값이 실제로 달라진 항목만 이력에 남긴다.
     *
     * @param reason 변경 사유 (API_SYNC / USER_REPORT / ADMIN)
     * @return 기록된 이력 건수
     */
    @Transactional
    public int updateWithHistory(Long placeId,
                                 String indoorStatus,
                                 String leashRequired,
                                 String muzzleRequired,
                                 String dangerousAllowed,
                                 BigDecimal maxWeight,
                                 String allowedSize,
                                 String reason) {

        PlaceCondition condition = conditionRepository.findByPlace_PlaceId(placeId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "출입 조건이 등록되지 않은 장소입니다. placeId=" + placeId));

        Place place = condition.getPlace();
        int recorded = 0;

        recorded += record(place, "indoorStatus",
                condition.getIndoorStatus(), indoorStatus, reason);
        recorded += record(place, "leashRequired",
                condition.getLeashRequired(), leashRequired, reason);
        recorded += record(place, "muzzleRequired",
                condition.getMuzzleRequired(), muzzleRequired, reason);
        recorded += record(place, "dangerousAllowed",
                condition.getDangerousAllowed(), dangerousAllowed, reason);
        recorded += record(place, "maxWeight",
                toText(condition.getMaxWeight()), toText(maxWeight), reason);
        recorded += record(place, "allowedSize",
                condition.getAllowedSize(), allowedSize, reason);

        condition.updateParsed(indoorStatus, leashRequired, muzzleRequired,
                dangerousAllowed, maxWeight, allowedSize);

        log.info("출입 조건 변경: placeId={}, 이력 {}건, 사유={}", placeId, recorded, reason);
        return recorded;
    }

    /** 값이 달라진 경우에만 이력을 저장한다 */
    private int record(Place place, String field,
                       String before, String after, String reason) {

        if (Objects.equals(before, after)) {
            return 0;
        }

        historyRepository.save(PolicyHistory.builder()
                .place(place)
                .field(field)
                .beforeValue(before)
                .afterValue(after)
                .reason(reason)
                .createdAt(LocalDateTime.now())
                .build());

        return 1;
    }

    /**
     * BigDecimal 값을 이력 저장용 문자열로 변환
     *
     * before_value, after_value가 VARCHAR이므로 무게 값도 문자열로 맞춘다.
     * toPlainString()을 쓰는 이유는 지수 표기(1E+2)를 피하기 위함이다.
     */
    private String toText(BigDecimal value) {
        return value == null ? null : value.toPlainString();
    }
    
    /**
     * 관광공사 API 갱신 시 출입 조건과 원문을 함께 반영한다.
     *
     * 파싱된 값의 변경 이력을 기록하고, 원문(rawText)도 최신으로 갱신한다.
     *
     * @return 기록된 이력 건수
     */
    @Transactional
    public int updateFromApi(Long placeId, PlaceCondition parsed) {

        int recorded = updateWithHistory(placeId,
                parsed.getIndoorStatus(),
                parsed.getLeashRequired(),
                parsed.getMuzzleRequired(),
                parsed.getDangerousAllowed(),
                parsed.getMaxWeight(),
                parsed.getAllowedSize(),
                "API_SYNC");

        // 원문은 이력 대상이 아니므로 조건 없이 갱신
        conditionRepository.findByPlace_PlaceId(placeId)
                .ifPresent(c -> c.updateRawText(parsed.getRawText()));

        return recorded;
    }
}