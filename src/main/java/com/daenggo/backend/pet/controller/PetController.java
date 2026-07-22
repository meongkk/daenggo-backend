package com.daenggo.backend.pet.controller;

import com.daenggo.backend.pet.dto.PetResponseDto;
import com.daenggo.backend.pet.service.PetService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@SecurityRequirement(name = "bearerAuth")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/pets")
public class PetController {

    private final PetService petService;

    @GetMapping
    public ResponseEntity<List<PetResponseDto.Summary>> getMyPets(
            final Authentication authentication
    ) {
        final List<PetResponseDto.Summary> response =
                petService.getMyPets(authentication.getName());
        return ResponseEntity.ok(response);
    }
}
