package com.daenggo.backend.place.controller;

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
import com.daenggo.backend.place.service.PlaceService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/places")
@RequiredArgsConstructor
public class PlaceController {

    private final PlaceService placeService;

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
}