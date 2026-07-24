package com.daenggo.backend.place.controller;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.daenggo.backend.place.dto.PlaceDetailResponse;
import com.daenggo.backend.place.dto.PlaceNearbyResponse;
import com.daenggo.backend.place.dto.PlaceSearchCondition;
import com.daenggo.backend.place.dto.PolicyChangeResponse;
import com.daenggo.backend.place.dto.RegionCountResponse;
import com.daenggo.backend.place.service.PlaceService;
import com.daenggo.backend.place.service.PolicyHistoryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/places")
@RequiredArgsConstructor
public class PlaceController {

    private final PlaceService placeService;
    private final PolicyHistoryService policyHistoryService;

    /** 지도 영역 + 조건 필터로 장소 목록 조회 (마커용) */
    @GetMapping("/nearby")
    public List<PlaceNearbyResponse> search(PlaceSearchCondition condition) {
        return placeService.search(condition);
    }
    
    /** 장소 상세 조회 (반려동물 출입 조건 포함) */
    @GetMapping("/{placeId}")
    public PlaceDetailResponse getPlaceDetail(@PathVariable Long placeId) {
        return placeService.findDetail(placeId);
    }
    
    /** 키워드로 장소 검색 (페이징) */
    @GetMapping("/search")
    public Page<PlaceNearbyResponse> searchByKeyword(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return placeService.searchByKeyword(keyword, page, size);
    }
    
    /**
     * 반려동물 맞춤 장소 조회
     *
     * 등록된 반려동물의 무게, 크기, 견종 정보를 기준으로 자동 필터링한다.
     */
    @GetMapping("/nearby/pet")
    public List<PlaceNearbyResponse> searchForPet(
            @RequestParam BigDecimal swLat,
            @RequestParam BigDecimal swLng,
            @RequestParam BigDecimal neLat,
            @RequestParam BigDecimal neLng,
            @RequestParam Long petId,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Boolean indoorAllowedOnly) {

        return placeService.searchForPet(
                swLat, swLng, neLat, neLng, petId, category, indoorAllowedOnly);
    }
    
    /** 지역별 장소 조회 */
    @GetMapping("/regions")
    public Page<PlaceNearbyResponse> findByRegion(
            @RequestParam String region,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return placeService.findByRegion(region, category, page, size);
    }
    
    /** 지역 목록 조회 (장소 개수 포함) */
    @GetMapping("/regions/list")
    public List<RegionCountResponse> getRegions() {
        return placeService.getRegions();
    }
    
    /** 장소 출입 규정 변경 이력 조회 */
    @GetMapping("/{placeId}/policy-history")
    public List<PolicyChangeResponse> getPolicyHistory(@PathVariable Long placeId) {
        return policyHistoryService.getHistory(placeId);
    }
}