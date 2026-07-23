package com.daenggo.backend.pet.controller;

import com.daenggo.backend.pet.dto.PetRequestDto;
import com.daenggo.backend.pet.dto.PetResponseDto;
import com.daenggo.backend.pet.service.PetService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

/**
 * 반려동물 정보 관리 REST 컨트롤러
 */
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/pets")
public class PetController {

    private final PetService petService;

    /**
     * 로그인 회원의 반려동물 목록 조회
     *
     * @param authentication 로그인 회원 인증 정보
     * @return 로그인 회원의 반려동물 목록 응답
     */
    @GetMapping
    public ResponseEntity<List<PetResponseDto.Summary>> getMyPets(
            final Authentication authentication
    ) {
        final List<PetResponseDto.Summary> response =
                petService.getMyPets(authentication.getName());
        return ResponseEntity.ok(response);
    }

    /**
     * 로그인 회원이 소유한 반려동물 상세 조회
     *
     * @param authentication 로그인 회원 인증 정보
     * @param petId 조회할 반려동물 ID
     * @return 반려동물 상세 정보 응답
     */
    @GetMapping("/{petId}")
    public ResponseEntity<PetResponseDto.Detail> getMyPet(
            final Authentication authentication,
            @PathVariable final Long petId
    ) {
        final PetResponseDto.Detail response = petService.getMyPet(
                authentication.getName(),
                petId
        );
        return ResponseEntity.ok(response);
    }

    /**
     * 로그인 회원의 반려동물 등록
     *
     * @param authentication 로그인 회원 인증 정보
     * @param request 반려동물 등록 요청
     * @return 등록된 반려동물 상세 정보 응답
     */
    @PostMapping
    public ResponseEntity<PetResponseDto.Detail> createPet(
            final Authentication authentication,
            @Valid @RequestBody final PetRequestDto.Create request
    ) {
        final PetResponseDto.Detail response = petService.createPet(
                authentication.getName(),
                request
        );
        return ResponseEntity
                .created(URI.create("/api/pets/" + response.getPetId()))
                .body(response);
    }

    /**
     * 로그인 회원이 소유한 반려동물 정보 수정
     *
     * @param authentication 로그인 회원 인증 정보
     * @param petId 수정할 반려동물 ID
     * @param request 반려동물 정보 수정 요청
     * @return 수정된 반려동물 상세 정보 응답
     */
    @PatchMapping("/{petId}")
    public ResponseEntity<PetResponseDto.Detail> updatePet(
            final Authentication authentication,
            @PathVariable final Long petId,
            @Valid @RequestBody final PetRequestDto.Update request
    ) {
        final PetResponseDto.Detail response = petService.updatePet(
                authentication.getName(),
                petId,
                request
        );
        return ResponseEntity.ok(response);
    }

    /**
     * 로그인 회원이 소유한 반려동물을 대표 반려동물로 설정
     *
     * @param authentication 로그인 회원 인증 정보
     * @param petId 대표로 설정할 반려동물 ID
     * @return 응답 본문 없는 성공 응답
     */
    @PatchMapping("/{petId}/primary")
    public ResponseEntity<Void> setPrimaryPet(
            final Authentication authentication,
            @PathVariable final Long petId
    ) {
        petService.setPrimaryPet(authentication.getName(), petId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 로그인 회원이 소유한 반려동물 삭제
     *
     * @param authentication 로그인 회원 인증 정보
     * @param petId 삭제할 반려동물 ID
     * @return 응답 본문 없는 성공 응답
     */
    @DeleteMapping("/{petId}")
    public ResponseEntity<Void> deletePet(
            final Authentication authentication,
            @PathVariable final Long petId
    ) {
        petService.deletePet(authentication.getName(), petId);
        return ResponseEntity.noContent().build();
    }
}
