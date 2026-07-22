package com.daenggo.backend.place.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.daenggo.backend.pet.entity.Pet;
import com.daenggo.backend.pet.repository.PetRepository;
import com.daenggo.backend.place.dto.PlaceDetailResponse;
import com.daenggo.backend.place.dto.PlaceNearbyResponse;
import com.daenggo.backend.place.dto.PlaceSearchCondition;
import com.daenggo.backend.place.dto.RegionCountResponse;
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
    private final PetRepository petRepository;

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
                cond.petSize(),
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
    
    /**
     * 등록된 반려동물 정보를 기준으로 장소 검색
     *
     * 반려동물의 무게, 크기, 견종(맹견 여부)을 조건으로 자동 적용한다.
     */
    public List<PlaceNearbyResponse> searchForPet(
            BigDecimal swLat, BigDecimal swLng,
            BigDecimal neLat, BigDecimal neLng,
            Long petId, String category, Boolean indoorAllowedOnly) {

        Pet pet = petRepository.findById(petId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "반려동물을 찾을 수 없습니다. petId=" + petId));

        boolean dangerous = pet.getBreed() != null && pet.getBreed().isDangerous();

        List<Place> places = placeRepository.searchByCondition(
                swLat, neLat, swLng, neLng,
                category,
                Boolean.TRUE.equals(indoorAllowedOnly),
                pet.getWeight(),
                pet.getSize(),
                dangerous);

        return places.stream()
                .map(PlaceNearbyResponse::from)
                .toList();
    }
    
    /**
     * 지역별 장소 조회
     *
     * 주소가 해당 지역으로 시작하는 장소를 반환한다.
     * 예) "서울" → 서울특별시 소재 장소
     */
    public Page<PlaceNearbyResponse> findByRegion(
            String region, String category, int page, int size) {

        if (region == null || region.isBlank()) {
            return Page.empty();
        }

        Pageable pageable = PageRequest.of(page, size);

        return placeRepository.findByRegion(region.trim(), category, pageable)
                .map(PlaceNearbyResponse::from);
    }
    
    /** 데이터가 존재하는 지역 목록 조회 (장소 개수 많은 순) */
    public List<RegionCountResponse> getRegions() {
        return placeRepository.findRegionCounts().stream()
                .map(row -> new RegionCountResponse(
                        (String) row[0],
                        ((Number) row[1]).longValue()))
                .toList();
    }
}