package com.daenggo.backend.pet.repository;

import com.daenggo.backend.pet.entity.Breed;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BreedRepository extends JpaRepository<Breed, Long> {

    Optional<Breed> findByName(String name);
}
