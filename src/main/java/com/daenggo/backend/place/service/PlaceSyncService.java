package com.daenggo.backend.place.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.daenggo.backend.place.api.TourApiClient;
import com.daenggo.backend.place.api.TourApiResponse;
import com.daenggo.backend.place.converter.PlaceConditionConverter;
import com.daenggo.backend.place.converter.PlaceConverter;
import com.daenggo.backend.place.converter.PlaceIntroConverter;
import com.daenggo.backend.place.entity.Place;
import com.daenggo.backend.place.entity.PlaceCondition;
import com.daenggo.backend.place.repository.PlaceConditionRepository;
import com.daenggo.backend.place.repository.PlaceRepository;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 관광공사 API 데이터를 우리 DB에 동기화
 *
 * 관광공사 API는 일 1,000건 호출 제한이 있어 사용자 요청마다 호출할 수 없다.
 * 미리 저장해두고 조회는 DB에서 처리한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PlaceSyncService {

    private final TourApiClient tourApiClient;
    private final PlaceConverter placeConverter;
    private final PlaceConditionConverter conditionConverter;
    private final PlaceIntroConverter introConverter;
    private final PlaceRepository placeRepository;
    private final PlaceConditionRepository conditionRepository;
    private final PolicyHistoryService policyHistoryService;

    /**
     * 주변 장소를 조회하여 저장하거나 갱신한다.
     *
     * 신규 장소는 저장하고, 기존 장소는 관광공사 수정일시가 더 최신인 경우에만
     * 출입 조건을 다시 조회하여 변경 사항을 반영하고 이력을 기록한다.
     *
     * @return 신규 저장된 장소 수
     */
    @Transactional
    public int syncNearbyPlaces(String mapX, String mapY, int radius, int numOfRows) {

        List<TourApiResponse.Item> items =
                tourApiClient.getNearbyPlaces(mapX, mapY, radius, numOfRows);

        int saved = 0;
        int updated = 0;

        for (TourApiResponse.Item item : items) {

            Place existing = placeRepository.findByContentId(item.contentid()).orElse(null);

            if (existing == null) {
                Place place = placeRepository.save(placeConverter.toEntity(item));
                saveCondition(place);
                saveIntro(place);
                saved++;
            } else if (isModified(existing, item)) {
                if (refreshCondition(existing, item)) {
                    updated++;
                }
            }
        }

        log.info("동기화 완료: 조회 {}건, 신규 {}건, 갱신 {}건", items.size(), saved, updated);
        return saved;
    }
    
    /** 관광공사 수정일시가 저장된 값보다 최신인지 확인 */
    private boolean isModified(Place place, TourApiResponse.Item item) {
        LocalDateTime apiModified = placeConverter.toDateTime(item.modifiedtime());
        if (apiModified == null || place.getApiModifiedAt() == null) {
            return false;
        }
        return apiModified.isAfter(place.getApiModifiedAt());
    }
    
    /**
     * 출입 조건을 다시 조회해 변경 사항을 반영하고 이력을 기록한다.
     *
     * 출입 조건이 아직 저장되지 않은 장소는 신규 저장으로 처리한다.
     *
     * @return 실제로 변경된 항목이 있으면 true
     */
    private boolean refreshCondition(Place place, TourApiResponse.Item item) {

        TourApiResponse.PetItem petItem =
                tourApiClient.getPetTourDetail(place.getContentId());
        if (petItem == null) {
            return false;
        }

        // 기존 조건이 없으면 신규 저장 (이력 대상 아님)
        if (!conditionRepository.existsByPlace_PlaceId(place.getPlaceId())) {
            conditionRepository.save(conditionConverter.toEntity(place, petItem));
            return false;
        }

        // 비교용으로 파싱만 수행 (저장하지 않음)
        PlaceCondition parsed = conditionConverter.toEntity(place, petItem);

        int recorded = policyHistoryService.updateFromApi(place.getPlaceId(), parsed);

        // 수정일시 갱신 (다음 동기화 시 중복 처리 방지)
        place.updateApiModifiedAt(placeConverter.toDateTime(item.modifiedtime()));

        return recorded > 0;
    }

    /**
     * 소개 정보가 비어 있는 기존 장소들의 전화번호·운영시간 등을 채운다.
     *
     * @param limit 한 번에 처리할 장소 수 (API 호출량 조절용)
     * @return 실제로 갱신된 장소 수
     */
    @Transactional
    public int syncMissingIntro(int limit) {

        List<Place> targets = placeRepository.findAll().stream()
                .filter(p -> p.getTel() == null && p.getOpenTime() == null)
                .limit(limit)
                .toList();

        int updated = 0;

        for (Place place : targets) {
            if (saveIntro(place)) {
                updated++;
            }
        }

        log.info("소개 정보 동기화: 대상 {}건, 갱신 {}건", targets.size(), updated);
        return updated;
    }

    /**
     * 출입 조건이 비어 있는 기존 장소들의 반려동물 조건을 채운다.
     *
     * @param limit 한 번에 처리할 장소 수
     * @return 실제로 저장된 조건 수
     */
    @Transactional
    public int syncMissingConditions(int limit) {

        List<Place> targets = placeRepository.findAll().stream()
                .filter(p -> !conditionRepository.existsByPlace_PlaceId(p.getPlaceId()))
                .limit(limit)
                .toList();

        int saved = 0;

        for (Place place : targets) {
            if (saveCondition(place)) {
                saved++;
            }
        }

        log.info("출입 조건 동기화: 대상 {}건, 저장 {}건", targets.size(), saved);
        return saved;
    }

    /**
     * 특정 장소의 반려동물 출입 조건 조회 및 저장
     *
     * @return 저장 여부 (API에 정보가 없으면 false)
     */
    private boolean saveCondition(Place place) {

        TourApiResponse.PetItem petItem =
                tourApiClient.getPetTourDetail(place.getContentId());

        if (petItem == null) {
            return false;
        }

        PlaceCondition condition = conditionConverter.toEntity(place, petItem);
        conditionRepository.save(condition);
        return true;
    }

    /**
     * 특정 장소의 소개 정보(전화번호, 운영시간, 휴무일, 주차) 조회 및 갱신
     *
     * @return 갱신 여부 (API에 정보가 없으면 false)
     */
    private boolean saveIntro(Place place) {

        String contentTypeId = introConverter.toContentTypeId(place.getCategory());
        if (contentTypeId == null) {
            return false;
        }

        JsonNode item = tourApiClient.getIntroDetail(place.getContentId(), contentTypeId);
        if (item == null) {
            return false;
        }

        String category = place.getCategory();
        place.updateIntro(
                introConverter.extractTel(item, category),
                introConverter.extractOpenTime(item, category),
                introConverter.extractRestDate(item, category),
                introConverter.extractParking(item, category));

        return true;
    }
    
    /**
     * 저장된 rawText로 출입 조건을 다시 파싱한다 (API 호출 없음)
     *
     * @return 재파싱된 건수
     */
    @Transactional
    public int reparseConditions() {
        List<PlaceCondition> all = conditionRepository.findAll();
        int count = 0;
        for (PlaceCondition c : all) {
            if (c.getRawText() != null && !c.getRawText().isBlank()) {
                conditionConverter.reparse(c);
                count++;
            }
        }
        log.info("출입 조건 재파싱: {}건", count);
        return count;
    }
}