package com.daenggo.backend.place.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.daenggo.backend.place.dto.PlaceDetailResponse;
import com.daenggo.backend.place.dto.PlaceNearbyResponse;
import com.daenggo.backend.place.dto.PlaceSearchCondition;
import com.daenggo.backend.place.entity.Place;
import com.daenggo.backend.place.entity.PlaceCondition;
import com.daenggo.backend.place.repository.PlaceConditionRepository;
import com.daenggo.backend.place.repository.PlaceRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlaceService {

    private final PlaceRepository placeRepository;
    private final PlaceConditionRepository conditionRepository;

    /** 장소 상세 조회 (반려동물 출입 조건 포함, 조건이 없으면 null) */
    public PlaceDetailResponse findDetail(Long placeId) {

        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "장소를 찾을 수 없습니다. placeId=" + placeId));

        // 출입 조건은 없을 수 있음 (API에 정보 미등록)
        PlaceCondition condition = conditionRepository
                .findByPlace_PlaceId(placeId)
                .orElse(null);

        return PlaceDetailResponse.from(place, condition);
    }
    
    /** 지도 영역 + 반려동물 조건으로 장소 검색 (지도 마커용) */
    public List<PlaceNearbyResponse> search(PlaceSearchCondition cond) {

        List<Place> places = placeRepository.searchByCondition(
                cond.swLat(), cond.neLat(), cond.swLng(), cond.neLng(),
                cond.category(),
                Boolean.TRUE.equals(cond.indoorAllowedOnly()),
                cond.petWeight(),
                Boolean.TRUE.equals(cond.isDangerous()));

        return places.stream()
                .map(PlaceNearbyResponse::from)
                .toList();
    }
    
    /** 키워드로 장소 검색 (장소명 우선 정렬, 페이징) */
    public Page<PlaceNearbyResponse> searchByKeyword(String keyword, int page, int size) {

        if (keyword == null || keyword.isBlank()) {
            return Page.empty();
        }

        Pageable pageable = PageRequest.of(page, size);

        return placeRepository.searchByKeyword(keyword.trim(), pageable)
                .map(PlaceNearbyResponse::from);
    }
}