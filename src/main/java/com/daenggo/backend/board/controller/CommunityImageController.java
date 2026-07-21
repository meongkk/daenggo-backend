package com.example.mingpark.controller;

import com.example.mingpark.service.ConcertImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 공연 이미지 컨트롤러
 * API:
 * POST /api/concert-images - 관리자 화면 공연 포스터 이미지 파일 업로드
 */
@RestController
@RequiredArgsConstructor
public class ConcertImageController {

    private final ConcertImageService concertImageService;

    /**
     * [POST] 관리자 화면에서 첨부한 공연 포스터 이미지를 서버 내부 저장소에 업로드 처리.
     * 성공 시 저장된 이미지 파일의 접근 URL 주소를 Map 형태로 반환.
     *
     * @param image 업로드할 멀티파트(MultipartFile) 이미지 파일 객체
     * @return 200 OK 응답 및 이미지 URL 주소(imageUrl) 반환, 파일 유효성 검증 실패 시 400 Bad Request 반환
     */
    @PostMapping("/api/concert-images")
    public ResponseEntity<?> uploadConcertImage(@RequestParam MultipartFile image) {
        try {
            String imageUrl = concertImageService.upload(image);
            return ResponseEntity.ok(Map.of("imageUrl", imageUrl));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}