package com.daenggo.backend.place.service;

import com.daenggo.backend.place.api.TourApiClient;
import com.daenggo.backend.place.api.TourApiResponse;
import com.daenggo.backend.place.converter.PlaceConverter;
import com.daenggo.backend.place.converter.PlaceConditionConverter;
import com.daenggo.backend.place.entity.Place;
import com.daenggo.backend.place.entity.PlaceCondition;
import com.daenggo.backend.place.repository.PlaceRepository;
import com.daenggo.backend.place.repository.PlaceConditionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

/**
 * 관광공사 API 데이터를 우리 DB에 동기화
 *
 * 사용자 요청마다 API를 호출하면 일일 호출 한도(1,000건)를 초과하므로
 * 미리 저장해두고 조회는 DB에서 처리한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PlaceSyncService {

    private final TourApiClient tourApiClient;
    private final PlaceConverter placeConverter;
    private final PlaceConditionConverter conditionConverter;
    private final PlaceRepository placeRepository;
    private final PlaceConditionRepository conditionRepository;

    /**
     * 주변 장소를 조회하여 저장하고, 각 장소의 출입 조건도 함께 저장
     *
     * 이미 저장된 장소(contentId 중복)는 건너뛴다.
     * @return 새로 저장된 장소 수
     */
    @Transactional
    public int syncNearbyPlaces(String mapX, String mapY, int radius, int numOfRows) {

        List<TourApiResponse.Item> items =
                tourApiClient.getNearbyPlaces(mapX, mapY, radius, numOfRows);

        int savedPlace = 0;
        int savedCondition = 0;

        for (TourApiResponse.Item item : items) {

            // 이미 저장된 장소면 건너뛰기
            if (placeRepository.existsByContentId(item.contentid())) {
                continue;
            }

            Place place = placeRepository.save(placeConverter.toEntity(item));
            savedPlace++;

            // 반려동물 출입 조건 조회 및 저장
            if (saveCondition(place)) {
                savedCondition++;
            }
        }

        log.info("동기화 완료: 조회 {}건, 장소 저장 {}건, 출입조건 저장 {}건",
                items.size(), savedPlace, savedCondition);
        return savedPlace;
    }

    /**
     * 특정 장소의 반려동물 출입 조건 조회 및 저장
     * @return 저장 성공 여부 (API에 정보가 없으면 false)
     */
    private boolean saveCondition(Place place) {
        TourApiResponse.PetItem petItem =
                tourApiClient.getPetTourDetail(place.getContentId());

        if (petItem == null) {
            log.debug("반려동물 정보 없음: contentId={}", place.getContentId());
            return false;
        }

        PlaceCondition condition = conditionConverter.toEntity(place, petItem);
        conditionRepository.save(condition);
        return true;
    }
}