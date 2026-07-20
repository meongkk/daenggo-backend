package com.daenggo.backend.place.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.FetchType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.AccessLevel;
import java.time.LocalDateTime;

// User 파일 추가 필요
//import com.daenggo.backend.user.entity.User;

@Entity
@Table(name = "place_report")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PlaceReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reportId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id", nullable = false)
    private Place place;                      

// User 파일 추가 필요
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "user_id", nullable = false)
//    private User user;
    
 // 임시: User 엔티티 병합 후 위에걸로 교체
    @Column(name = "user_id", nullable = false)
    private Long userId;  

    @Column(length = 50, nullable = false)
    private String reportType;                 

    @Column(length = 500)
    private String content;                   

    @Column(length = 20, nullable = false)
    @Builder.Default
    private String status = "PENDING";         // PENDING/APPROVED/REJECTED

    @Column(nullable = false)
    private LocalDateTime createdAt;
}