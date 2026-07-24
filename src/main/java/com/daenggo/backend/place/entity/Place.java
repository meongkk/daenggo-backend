package com.daenggo.backend.place.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.AccessLevel;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "place")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Place {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long placeId;                    

    @Column(unique = true, length = 20)
    private String contentId;              

    @Column(length = 100, nullable = false)
    private String title;                   

    @Column(length = 20, nullable = false)
    private String category;                

    @Column(length = 255)
    private String address;                 

    @Column(precision = 10, scale = 7)
    private BigDecimal latitude;        

    @Column(precision = 10, scale = 7)
    private BigDecimal longitude;           

    @Column(length = 100)
    private String tel;
    
    @Column(length = 500)
    private String openTime;               // 운영시간 / 체크인·아웃

    @Column(length = 200)
    private String restDate;               // 휴무일

    @Column(length = 100)
    private String parking;                // 주차 가능 여부

    @Column(length = 500)
    private String thumbnail;                // 대표 이미지 (firstimage)

    private LocalDateTime apiModifiedAt;     // API 수정일시 (modifiedtime)

    private LocalDateTime createdAt;         // DB 저장일시
    
    /** 관광공사 소개 정보(detailIntro2)로 상세 항목을 채운다 */
    public void updateIntro(String tel, String openTime, String restDate, String parking) {
        this.tel = tel;
        this.openTime = openTime;
        this.restDate = restDate;
        this.parking = parking;
    }
    
    /** 관광공사 수정일시 갱신 (동기화 시) */
    public void updateApiModifiedAt(LocalDateTime apiModifiedAt) {
        this.apiModifiedAt = apiModifiedAt;
    }
}