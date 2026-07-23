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
import com.daenggo.backend.user.entity.User;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(length = 50, nullable = false)
    private String reportType;                 

    @Column(length = 500)
    private String content;                   

    @Column(length = 20, nullable = false)
    @Builder.Default
    private String status = "PENDING";         // PENDING/APPROVED/REJECTED

    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    /** 신고 내용 수정 (대기 상태일 때만 호출) */
    public void update(String reportType, String content) {
        this.reportType = reportType;
        this.content = content;
    }

    /** 대기 상태인지 확인 (수정·취소 가능 여부 판단용) */
    public boolean isPending() {
        return "PENDING".equals(this.status);
    }
}