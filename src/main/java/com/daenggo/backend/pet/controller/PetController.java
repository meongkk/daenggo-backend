package com.daenggo.backend.pet.controller;

import com.daenggo.backend.pet.dto.PetRequestDto;
import com.daenggo.backend.pet.dto.PetResponseDto;
import com.daenggo.backend.pet.service.PetService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
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
}
