package com.daenggo.backend.board.controller;

import com.daenggo.backend.board.service.CommunityImageService;
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
 * 커뮤니티 이미지 업로드와 조회 요청을 처리하는 컨트롤러.
 */
@RestController
@RequestMapping("/api/community/images")
public class CommunityImageController {

    /** 커뮤니티 이미지 저장 서비스. */
    private final CommunityImageService communityImageService;

    /**
     * 커뮤니티 이미지 컨트롤러를 생성한다.
     *
     * @param communityImageService 커뮤니티 이미지 저장 서비스
     */
    public CommunityImageController(CommunityImageService communityImageService) {
        this.communityImageService = communityImageService;
    }

    /**
     * 멀티파트 이미지 파일을 로컬 저장소에 저장한다.
     *
     * @param image 업로드할 이미지 파일
     * @return 저장된 이미지를 조회할 수 있는 URL
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> uploadImage(
            @RequestParam(name = "image") MultipartFile image
    ) {
        try {
            String imageUrl = communityImageService.upload(image);

            return ResponseEntity
                    .created(URI.create(imageUrl))
                    .body(Map.of("imageUrl", imageUrl));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", exception.getMessage()));
        } catch (IllegalStateException exception) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("message", "이미지 저장 중 오류가 발생했습니다."));
        }
    }

    /**
     * 저장된 이미지 파일을 조회한다.
     *
     * @param fileName 조회할 이미지 파일명
     * @return 이미지 파일 데이터
     */
    @GetMapping("/{fileName:.+}")
    public ResponseEntity<Resource> getImage(@PathVariable String fileName) {
        Resource image = communityImageService.load(fileName);
        MediaType mediaType = MediaTypeFactory.getMediaType(image)
                .orElse(MediaType.APPLICATION_OCTET_STREAM);

        return ResponseEntity.ok()
                .contentType(mediaType)
                .body(image);
    }
}
