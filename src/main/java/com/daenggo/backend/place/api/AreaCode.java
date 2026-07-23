package com.daenggo.backend.place.api;

import java.util.List;

/**
 * 관광공사 지역 코드
 *
 * areaCode2 API로 조회한 값을 상수로 관리.
 * 지역 구분이 바뀌는 일이 드물어 하드코딩해도 무방.
 */
public record AreaCode(String code, String name) {

    public static final List<AreaCode> ALL = List.of(
            new AreaCode("1",  "서울"),
            new AreaCode("2",  "인천"),
            new AreaCode("3",  "대전"),
            new AreaCode("4",  "대구"),
            new AreaCode("5",  "광주"),
            new AreaCode("6",  "부산"),
            new AreaCode("7",  "울산"),
            new AreaCode("8",  "세종특별자치시"),
            new AreaCode("31", "경기도"),
            new AreaCode("32", "강원특별자치도"),
            new AreaCode("33", "충청북도"),
            new AreaCode("34", "충청남도"),
            new AreaCode("35", "경상북도"),
            new AreaCode("36", "경상남도"),
            new AreaCode("37", "전북특별자치도"),
            new AreaCode("38", "전라남도"),
            new AreaCode("39", "제주특별자치도")
    );
}