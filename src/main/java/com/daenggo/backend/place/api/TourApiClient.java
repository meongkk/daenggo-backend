package com.daenggo.backend.place.api;

import java.net.URI;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 한국관광공사 반려동물 동반여행 API 호출 클라이언트
 *
 * base-url은 반드시 https를 사용. (http 요청 시 502 발생)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TourApiClient {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    @Value("${tour.api.base-url}")
    private String baseUrl;

    @Value("${tour.api.service-key}")
    private String serviceKey;

    /** 위치 기반 반려동물 동반 장소 목록 조회 */
    public List<TourApiResponse.Item> getNearbyPlaces(
            String mapX, String mapY, int radius, int numOfRows) {

        URI uri = UriComponentsBuilder
                .fromUriString(baseUrl + "/locationBasedList2")
                .queryParam("serviceKey", serviceKey)
                .queryParam("MobileOS", "ETC")
                .queryParam("MobileApp", "daenggo")
                .queryParam("mapX", mapX)
                .queryParam("mapY", mapY)
                .queryParam("radius", radius)
                .queryParam("numOfRows", numOfRows)
                .queryParam("_type", "json")
                .build(true)          // 서비스키를 이중 인코딩하지 않도록
                .toUri();
        
        log.info("요청 URL: {}", uri); 

        TourApiResponse response = restClient.get()
                .uri(uri)
                .retrieve()
                .body(TourApiResponse.class);

        return extractItems(response);
    }

    /** 응답에서 item 목록 추출 (resultCode가 0000이 아니면 빈 목록 반환) */
    private List<TourApiResponse.Item> extractItems(TourApiResponse response) {
        if (response == null || response.response() == null) {
            log.warn("관광공사 API 응답이 비어 있습니다.");
            return Collections.emptyList();
        }

        String resultCode = response.response().header().resultCode();
        if (!"0000".equals(resultCode)) {
            log.error("관광공사 API 오류: {} - {}",
                    resultCode, response.response().header().resultMsg());
            return Collections.emptyList();
        }

        var body = response.response().body();
        if (body == null || body.items() == null || body.items().item() == null) {
            return Collections.emptyList();
        }
        return body.items().item();
    }
    

    /**
     * 반려동물 동반 상세 정보 조회
     * 정보가 등록되지 않은 장소는 null 반환
     */
    public TourApiResponse.PetItem getPetTourDetail(String contentId) {

        URI uri = UriComponentsBuilder
                .fromUriString(baseUrl + "/detailPetTour2")
                .queryParam("serviceKey", serviceKey)
                .queryParam("MobileOS", "ETC")
                .queryParam("MobileApp", "daenggo")
                .queryParam("contentId", contentId)
                .queryParam("_type", "json")
                .build(true)
                .toUri();

        try {
            String json = restClient.get().uri(uri).retrieve().body(String.class);

            JsonNode items = objectMapper.readTree(json)
                    .path("response").path("body").path("items");

            // items가 빈 문자열("")이면 반려동물 정보 없음
            if (!items.isObject()) {
                return null;
            }

            JsonNode item = items.path("item");
            if (!item.isArray() || item.isEmpty()) {
                return null;
            }

            return objectMapper.treeToValue(item.get(0), TourApiResponse.PetItem.class);

        } catch (Exception e) {
            log.warn("반려동물 상세 조회 실패 contentId={}", contentId, e);
            return null;
        }
    }
    
    /**
     * 장소 소개 정보 조회 (detailIntro2)
     *
     * 응답 필드명이 contentTypeId마다 다르므로 JsonNode로 직접 다룬다.
     * 정보가 없으면 null을 반환한다.
     */
    public JsonNode getIntroDetail(String contentId, String contentTypeId) {

        URI uri = UriComponentsBuilder
                .fromUriString(baseUrl + "/detailIntro2")
                .queryParam("serviceKey", serviceKey)
                .queryParam("MobileOS", "ETC")
                .queryParam("MobileApp", "daenggo")
                .queryParam("contentId", contentId)
                .queryParam("contentTypeId", contentTypeId)
                .queryParam("_type", "json")
                .build(true)
                .toUri();

        try {
            String json = restClient.get().uri(uri).retrieve().body(String.class);
            JsonNode items = objectMapper.readTree(json)
                    .path("response").path("body").path("items");

            if (!items.isObject()) return null;

            JsonNode item = items.path("item");
            if (!item.isArray() || item.isEmpty()) return null;

            return item.get(0);

        } catch (Exception e) {
            log.warn("소개 정보 조회 실패 contentId={}", contentId, e);
            return null;
        }
    }
}