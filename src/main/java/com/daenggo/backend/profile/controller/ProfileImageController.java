package com.daenggo.backend.profile.controller;

import com.daenggo.backend.profile.service.ProfileImageService;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.util.Map;

/**
 * 유저와 펫의 프로필 이미지 업로드 및 조회 요청을 처리한다.
 */
@RestController
public class ProfileImageController {

    private final ProfileImageService profileImageService;

    public ProfileImageController(final ProfileImageService profileImageService) {
        this.profileImageService = profileImageService;
    }

    /**
     * 유저 프로필 이미지를 저장한다.
     */
    @PostMapping(
            value = "/api/users/images",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<Map<String, String>> uploadUserImage(
            @RequestParam(name = "image") final MultipartFile image
    ) {
        return created(profileImageService.uploadUserImage(image));
    }

    /**
     * 펫 프로필 이미지를 저장한다.
     */
    @PostMapping(
            value = "/api/pets/images",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<Map<String, String>> uploadPetImage(
            @RequestParam(name = "image") final MultipartFile image
    ) {
        return created(profileImageService.uploadPetImage(image));
    }

    /**
     * 저장된 유저 프로필 이미지를 조회한다.
     */
    @GetMapping("/api/users/images/{fileName:.+}")
    public ResponseEntity<Resource> getUserImage(@PathVariable final String fileName) {
        return image(profileImageService.loadUserImage(fileName));
    }

    /**
     * 저장된 펫 프로필 이미지를 조회한다.
     */
    @GetMapping("/api/pets/images/{fileName:.+}")
    public ResponseEntity<Resource> getPetImage(@PathVariable final String fileName) {
        return image(profileImageService.loadPetImage(fileName));
    }

    private ResponseEntity<Map<String, String>> created(final String imageUrl) {
        return ResponseEntity
                .created(URI.create(imageUrl))
                .body(Map.of("imageUrl", imageUrl));
    }

    private ResponseEntity<Resource> image(final Resource image) {
        final MediaType mediaType = MediaTypeFactory.getMediaType(image)
                .orElse(MediaType.APPLICATION_OCTET_STREAM);

        return ResponseEntity.ok()
                .contentType(mediaType)
                .body(image);
    }
}
