package com.daenggo.backend.pet.repository;

import com.daenggo.backend.pet.entity.Pet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PetRepository extends JpaRepository<Pet, Long> {

    List<Pet> findAllByUserIdAndDeletedAtIsNull(Long userId);

    Optional<Pet> findByIdAndUserIdAndDeletedAtIsNull(Long petId, Long userId);

    Optional<Pet> findByUserIdAndPrimaryTrueAndDeletedAtIsNull(Long userId);

    Optional<Pet> findByIdAndDeletedAtIsNull(Long petId);

    boolean existsByIdAndUserIdAndDeletedAtIsNull(Long petId, Long userId);
}
