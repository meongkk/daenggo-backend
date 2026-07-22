package com.daenggo.backend.pet.service;

import com.daenggo.backend.pet.dto.PetRequestDto;
import com.daenggo.backend.pet.dto.PetResponseDto;
import com.daenggo.backend.pet.entity.Breed;
import com.daenggo.backend.pet.entity.Pet;
import com.daenggo.backend.pet.repository.BreedRepository;
import com.daenggo.backend.pet.repository.PetRepository;
import com.daenggo.backend.user.entity.User;
import com.daenggo.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PetService {

    private static final String DIRECT_INPUT_BREED_NAME = "직접 입력";

    private final PetRepository petRepository;
    private final BreedRepository breedRepository;
    private final UserRepository userRepository;

    public List<PetResponseDto.Summary> getMyPets(final String email) {
        final User user = userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "회원을 찾을 수 없습니다."
                ));

        return petRepository.findAllByUserId(user.getId())
                .stream()
                .map(PetResponseDto.Summary::from)
                .toList();
    }

    @Transactional
    public PetResponseDto.Detail createPet(
            final String email,
            final PetRequestDto.Create request
    ) {
        final User user = userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "회원을 찾을 수 없습니다."
                ));
        final ResolvedBreed resolvedBreed = resolveBreed(request);
        final boolean primary = Boolean.TRUE.equals(request.getPrimary());

        if (primary) {
            petRepository.findByUserIdAndPrimaryTrue(user.getId())
                    .ifPresent(Pet::removePrimary);
        }

        final Pet pet = Pet.builder()
                .user(user)
                .breed(resolvedBreed.breed())
                .primary(primary)
                .name(request.getName().trim())
                .weight(request.getWeight())
                .size(request.getSize().trim())
                .image(normalizeNullable(request.getProfileImageUrl()))
                .registrationNumber(normalizeNullable(request.getRegistrationNumber()))
                .vaccine(normalizeNullable(request.getVaccine()))
                .breedText(resolvedBreed.breedText())
                .build();
        final Pet savedPet = petRepository.saveAndFlush(pet);

        return PetResponseDto.Detail.from(savedPet);
    }

    private ResolvedBreed resolveBreed(final PetRequestDto.Create request) {
        final Long breedId = request.getBreedId();
        final String breedText = normalizeNullable(request.getBreedText());

        if (breedId != null && breedText != null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "견종 선택과 직접 입력은 동시에 사용할 수 없습니다."
            );
        }

        if (breedId != null) {
            final Breed breed = breedRepository.findById(breedId)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "선택한 견종을 찾을 수 없습니다."
                    ));
            return new ResolvedBreed(breed, null);
        }

        if (breedText == null) {
            return new ResolvedBreed(null, null);
        }

        final Breed directInputBreed = breedRepository.findByName(DIRECT_INPUT_BREED_NAME)
                .orElseGet(() -> breedRepository.save(Breed.builder()
                        .name(DIRECT_INPUT_BREED_NAME)
                        .dangerous(false)
                        .build()));
        return new ResolvedBreed(directInputBreed, breedText);
    }

    private String normalizeNullable(final String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private record ResolvedBreed(Breed breed, String breedText) {
    }
}
