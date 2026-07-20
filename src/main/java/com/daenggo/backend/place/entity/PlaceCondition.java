package com.daenggo.backend.place.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import jakarta.persistence.OneToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.FetchType;
import jakarta.persistence.Lob;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.AccessLevel;
import java.math.BigDecimal;

@Entity
@Table(name = "place_condition")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PlaceCondition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ruleId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id", nullable = false, unique = true)
    private Place place;                      

    @Column(length = 20)
    private String allowedSize;               

    @Column(precision = 5, scale = 2)
    private BigDecimal maxWeight;             

    @Column(length = 20, nullable = false)
    @Builder.Default
    private String indoorStatus = "UNKNOWN";  

    @Column(length = 20, nullable = false)
    @Builder.Default
    private String leashRequired = "UNKNOWN";  

    @Column(length = 20, nullable = false)
    @Builder.Default
    private String muzzleRequired = "UNKNOWN"; 

    @Column(length = 20, nullable = false)
    @Builder.Default
    private String dangerousAllowed = "UNKNOWN"; 

    @Column(length = 500)
    private String amenities;                  

    @Lob
    private String rawText;                   
}