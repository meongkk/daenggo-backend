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
