package com.daenggo.backend.pet.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "breeds")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Breed {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "breed_id")
    private Long id;

    @Column(name = "breed_name", nullable = false, length = 50)
    private String name;

    @Column(name = "is_dangerous", nullable = false)
    private boolean dangerous;

    @Builder
    private Breed(String name, boolean dangerous) {
        this.name = name;
        this.dangerous = dangerous;
    }
}
