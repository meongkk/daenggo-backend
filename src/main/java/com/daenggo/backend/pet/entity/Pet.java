package com.daenggo.backend.pet.entity;

import com.daenggo.backend.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "pets")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Pet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pet_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "breed_id")
    private Breed breed;

    @Column(name = "is_primary", nullable = false)
    private boolean primary;

    @Column(name = "pet_name", nullable = false, length = 50)
    private String name;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal weight;

    @Column(nullable = false, length = 20)
    private String size;

    @Column(length = 500)
    private String image;

    @Column(name = "reg_no", length = 50)
    private String registrationNumber;

    @Column(length = 50)
    private String vaccine;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "breed_text", length = 50)
    private String breedText;

    @Builder
    private Pet(
            User user,
            Breed breed,
            boolean primary,
            String name,
            BigDecimal weight,
            String size,
            String image,
            String registrationNumber,
            String vaccine,
            String breedText
    ) {
        this.user = user;
        this.breed = breed;
        this.primary = primary;
        this.name = name;
        this.weight = weight;
        this.size = size;
        this.image = image;
        this.registrationNumber = registrationNumber;
        this.vaccine = vaccine;
        this.breedText = breedText;
    }

    public void makePrimary() {
        this.primary = true;
    }

    public void removePrimary() {
        this.primary = false;
    }

    /**
     * 반려동물의 필수 기본 정보 수정
     *
     * @param name 변경할 이름
     * @param weight 변경할 몸무게
     * @param size 변경할 크기
     */
    public void updateBasicInfo(
            final String name,
            final BigDecimal weight,
            final String size
    ) {
        if (name != null) {
            this.name = name;
        }
        if (weight != null) {
            this.weight = weight;
        }
        if (size != null) {
            this.size = size;
        }
    }

    /**
     * 반려동물의 견종 정보 수정
     *
     * @param breed 변경할 견종 엔티티
     * @param breedText 직접 입력한 견종명
     */
    public void updateBreed(final Breed breed, final String breedText) {
        this.breed = breed;
        this.breedText = breedText;
    }

    /**
     * 반려동물 프로필 이미지 URL 수정
     *
     * @param image 변경할 이미지 URL
     */
    public void updateProfileImage(final String image) {
        this.image = image;
    }

    /**
     * 반려동물 등록번호 수정
     *
     * @param registrationNumber 변경할 동물등록번호
     */
    public void updateRegistrationNumber(final String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    /**
     * 반려동물 예방접종 정보 수정
     *
     * @param vaccine 변경할 예방접종 정보
     */
    public void updateVaccine(final String vaccine) {
        this.vaccine = vaccine;
    }

    /**
     * 반려동물 삭제 상태 기록
     */
    public void softDelete() {
        this.primary = false;
        this.deletedAt = LocalDateTime.now();
    }
}
