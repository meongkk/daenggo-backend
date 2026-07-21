package com.daenggo.backend.pet.repository;

import com.daenggo.backend.pet.entity.Pet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PetRepository extends JpaRepository<Pet, Long> {

    List<Pet> findAllByUserId(Long userId);

    Optional<Pet> findByIdAndUserId(Long petId, Long userId);

    boolean existsByIdAndUserId(Long petId, Long userId);
}
