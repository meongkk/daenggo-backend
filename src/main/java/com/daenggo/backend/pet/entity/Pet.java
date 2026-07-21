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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "breed_id", nullable = false)
    private Breed breed;

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

    @Column(name = "breed_text", length = 50)
    private String breedText;

    @Builder
    private Pet(
            User user,
            Breed breed,
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
        this.name = name;
        this.weight = weight;
        this.size = size;
        this.image = image;
        this.registrationNumber = registrationNumber;
        this.vaccine = vaccine;
        this.breedText = breedText;
    }
}
