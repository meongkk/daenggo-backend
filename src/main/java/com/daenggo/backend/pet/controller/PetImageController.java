package com.daenggo.backend.pet.controller;

import com.daenggo.backend.profile.service.ProfileImageService;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.util.Map;

/**
 * 펫 이미지 업로드 및 조회 요청을 처리한다.
 */
@RestController
@RequestMapping("/api/pets/images")
public class PetImageController {

    private final ProfileImageService profileImageService;

    public PetImageController(final ProfileImageService profileImageService) {
        this.profileImageService = profileImageService;
    }

    /**
     * 펫 이미지를 로컬 저장소에 저장한다.
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> uploadImage(
            @RequestParam(name = "image") final MultipartFile image
    ) {
        final String imageUrl = profileImageService.uploadPetImage(image);

        return ResponseEntity
                .created(URI.create(imageUrl))
                .body(Map.of("imageUrl", imageUrl));
    }

    /**
     * 저장된 펫 이미지를 조회한다.
     */
    @GetMapping("/{fileName:.+}")
    public ResponseEntity<Resource> getImage(@PathVariable final String fileName) {
        final Resource image = profileImageService.loadPetImage(fileName);
        final MediaType mediaType = MediaTypeFactory.getMediaType(image)
                .orElse(MediaType.APPLICATION_OCTET_STREAM);

        return ResponseEntity.ok()
                .contentType(mediaType)
                .body(image);
    }
}
