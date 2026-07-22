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

/**
 * 반려동물 정보 관리 비즈니스 로직
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PetService {

    private static final String DIRECT_INPUT_BREED_NAME = "직접 입력";

    private final PetRepository petRepository;
    private final BreedRepository breedRepository;
    private final UserRepository userRepository;

    /**
     * 로그인 회원의 반려동물 목록 조회
     *
     * @param email 로그인 회원 이메일
     * @return 로그인 회원의 반려동물 목록
     */
    public List<PetResponseDto.Summary> getMyPets(final String email) {
        final User user = findActiveUser(email);

        return petRepository.findAllByUserId(user.getId())
                .stream()
                .map(PetResponseDto.Summary::from)
                .toList();
    }

    /**
     * 로그인 회원이 소유한 반려동물 상세 조회
     *
     * @param email 로그인 회원 이메일
     * @param petId 조회할 반려동물 ID
     * @return 반려동물 상세 정보
     */
    public PetResponseDto.Detail getMyPet(final String email, final Long petId) {
        final User user = findActiveUser(email);
        final Pet pet = petRepository.findByIdAndUserId(petId, user.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "반려동물을 찾을 수 없습니다."
                ));

        return PetResponseDto.Detail.from(pet);
    }

    /**
     * 로그인 회원의 반려동물 등록
     *
     * @param email 로그인 회원 이메일
     * @param request 반려동물 등록 요청
     * @return 등록된 반려동물 상세 정보
     */
    @Transactional
    public PetResponseDto.Detail createPet(
            final String email,
            final PetRequestDto.Create request
    ) {
        final User user = findActiveUser(email);
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

    /**
     * 활성 회원 조회
     *
     * @param email 로그인 회원 이메일
     * @return 활성 회원 엔티티
     */
    private User findActiveUser(final String email) {
        return userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "회원을 찾을 수 없습니다."
                ));
    }

    /**
     * 등록 요청의 견종 선택 또는 직접 입력 정보 처리
     *
     * @param request 반려동물 등록 요청
     * @return 저장할 견종 엔티티와 직접 입력 견종명
     */
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

    /**
     * 선택 입력 문자열의 앞뒤 공백 제거 및 빈 문자열 정규화
     *
     * @param value 정규화할 문자열
     * @return 공백을 제거한 문자열 또는 값이 없으면 null
     */
    private String normalizeNullable(final String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    /**
     * 반려동물 등록에 사용할 견종 처리 결과
     *
     * @param breed 저장할 견종 엔티티
     * @param breedText 직접 입력한 견종명
     */
    private record ResolvedBreed(Breed breed, String breedText) {
    }
}
