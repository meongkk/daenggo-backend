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
        final Pet pet = findOwnedPet(petId, user.getId());

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
     * 로그인 회원이 소유한 반려동물 정보 수정
     *
     * @param email 로그인 회원 이메일
     * @param petId 수정할 반려동물 ID
     * @param request 반려동물 정보 수정 요청
     * @return 수정된 반려동물 상세 정보
     */
    @Transactional
    public PetResponseDto.Detail updatePet(
            final String email,
            final Long petId,
            final PetRequestDto.Update request
    ) {
        final User user = findActiveUser(email);
        final Pet pet = findOwnedPet(petId, user.getId());
        final ResolvedBreed resolvedBreed = resolveBreed(request);

        pet.updateBasicInfo(
                normalizeNullable(request.getName()),
                request.getWeight(),
                normalizeNullable(request.getSize())
        );

        if (resolvedBreed != null) {
            pet.updateBreed(resolvedBreed.breed(), resolvedBreed.breedText());
        }
        if (request.getProfileImageUrl() != null) {
            pet.updateProfileImage(normalizeNullable(request.getProfileImageUrl()));
        }
        if (request.getRegistrationNumber() != null) {
            pet.updateRegistrationNumber(normalizeNullable(request.getRegistrationNumber()));
        }
        if (request.getVaccine() != null) {
            pet.updateVaccine(normalizeNullable(request.getVaccine()));
        }

        return PetResponseDto.Detail.from(pet);
    }

    /**
     * 로그인 회원이 소유한 반려동물을 대표 반려동물로 설정
     *
     * @param email 로그인 회원 이메일
     * @param petId 대표로 설정할 반려동물 ID
     */
    @Transactional
    public void setPrimaryPet(final String email, final Long petId) {
        final User user = findActiveUser(email);
        final Pet pet = findOwnedPet(petId, user.getId());

        if (pet.isPrimary()) {
            return;
        }

        petRepository.findByUserIdAndPrimaryTrue(user.getId()) // 기존 대표 반려동물 해제
                .ifPresent(Pet::removePrimary);
        pet.makePrimary();
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
     * 로그인 회원이 소유한 반려동물 조회
     *
     * @param petId 조회할 반려동물 ID
     * @param userId 로그인 회원 ID
     * @return 로그인 회원이 소유한 반려동물 엔티티
     */
    private Pet findOwnedPet(final Long petId, final Long userId) {
        return petRepository.findByIdAndUserId(petId, userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "반려동물을 찾을 수 없습니다."
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
                .orElseGet(this::createDirectInputBreed);
        return new ResolvedBreed(directInputBreed, breedText);
    }

    /**
     * 수정 요청의 견종 변경 정보 처리
     *
     * @param request 반려동물 정보 수정 요청
     * @return 변경할 견종 정보 또는 견종 변경 요청이 없으면 null
     */
    private ResolvedBreed resolveBreed(final PetRequestDto.Update request) {
        final Long breedId = request.getBreedId();
        final String rawBreedText = request.getBreedText();

        if (breedId == null && rawBreedText == null) {
            return null;
        }

        final String breedText = normalizeNullable(rawBreedText);
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
                .orElseGet(this::createDirectInputBreed);
        return new ResolvedBreed(directInputBreed, breedText);
    }

    /**
     * 직접 입력 견종을 연결하기 위한 공통 견종 엔티티 생성
     *
     * @return 생성된 직접 입력 견종 엔티티
     */
    private Breed createDirectInputBreed() {
        return breedRepository.save(Breed.builder()
                .name(DIRECT_INPUT_BREED_NAME)
                .dangerous(false)
                .build());
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
