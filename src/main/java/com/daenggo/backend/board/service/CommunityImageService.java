package com.daenggo.backend.board.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * 커뮤니티 이미지 파일의 검증, 저장, 조회를 처리하는 서비스.
 */
@Service
public class CommunityImageService {

    /** 이미지 조회 API의 기본 경로. */
    private static final String IMAGE_API_PATH = "/api/community/images/";

    /** MIME 타입별 저장 확장자. */
    private static final Map<String, String> IMAGE_EXTENSIONS = Map.of(
            "image/jpeg", ".jpg",
            "image/png", ".png",
            "image/gif", ".gif",
            "image/webp", ".webp"
    );

    /** 서비스가 생성한 이미지 파일명 형식. */
    private static final Pattern SAFE_FILE_NAME = Pattern.compile(
            "^[0-9a-fA-F-]{36}\\.(jpg|png|gif|webp)$"
    );

    /** 이미지 파일을 저장할 절대 경로. */
    private final Path uploadDirectory;

    /**
     * 설정값을 기준으로 이미지 저장 경로를 생성한다.
     *
     * @param uploadDirectory 이미지 저장 디렉터리 경로
     */
    public CommunityImageService(
            @Value("${app.community-image.upload-directory:uploads}") String uploadDirectory
    ) {
        this.uploadDirectory = Path.of(uploadDirectory).toAbsolutePath().normalize();
    }

    /**
     * 이미지 파일을 검증하고 UUID 파일명으로 저장한다.
     *
     * @param image 저장할 멀티파트 이미지 파일
     * @return 저장된 이미지의 조회 URL
     * @throws IllegalArgumentException 파일이 비어 있거나 지원하지 않는 이미지인 경우
     * @throws IllegalStateException 파일 저장에 실패한 경우
     */
    public String upload(MultipartFile image) {
        String extension = validateAndGetExtension(image);
        String fileName = UUID.randomUUID() + extension;
        Path targetPath = uploadDirectory.resolve(fileName).normalize();

        try {
            Files.createDirectories(uploadDirectory);

            try (InputStream inputStream = image.getInputStream()) {
                Files.copy(inputStream, targetPath);
            }
        } catch (IOException exception) {
            deleteIncompleteFile(targetPath, exception);
            throw new IllegalStateException("이미지 저장에 실패했습니다.", exception);
        }

        return IMAGE_API_PATH + fileName;
    }

    /**
     * 파일명에 해당하는 저장 이미지를 조회한다.
     *
     * @param fileName 조회할 이미지 파일명
     * @return 조회 가능한 이미지 리소스
     * @throws ResponseStatusException 파일명이 올바르지 않거나 이미지가 없는 경우
     */
    public Resource load(String fileName) {
        if (fileName == null || !SAFE_FILE_NAME.matcher(fileName).matches()) {
            throw imageNotFound();
        }

        Path imagePath = uploadDirectory.resolve(fileName).normalize();

        if (!imagePath.startsWith(uploadDirectory) || !Files.isRegularFile(imagePath)) {
            throw imageNotFound();
        }

        return new FileSystemResource(imagePath);
    }

    /**
     * 업로드 파일의 MIME 타입과 실제 파일 헤더를 검증한다.
     *
     * @param image 검증할 이미지 파일
     * @return 검증된 이미지의 저장 확장자
     */
    private String validateAndGetExtension(MultipartFile image) {
        if (image == null || image.isEmpty()) {
            throw new IllegalArgumentException("이미지 파일을 선택해주세요.");
        }

        String contentType = image.getContentType();
        String normalizedContentType = contentType == null
                ? ""
                : contentType.toLowerCase(Locale.ROOT);
        String extension = IMAGE_EXTENSIONS.get(normalizedContentType);

        if (extension == null || !hasValidSignature(image, normalizedContentType)) {
            throw new IllegalArgumentException(
                    "JPG, PNG, GIF, WEBP 형식의 이미지만 업로드할 수 있습니다."
            );
        }

        return extension;
    }

    /**
     * MIME 타입과 파일 시작 바이트가 일치하는지 확인한다.
     *
     * @param image 검사할 이미지 파일
     * @param contentType 정규화된 MIME 타입
     * @return 파일 헤더가 이미지 형식과 일치하면 true
     */
    private boolean hasValidSignature(MultipartFile image, String contentType) {
        try (InputStream inputStream = image.getInputStream()) {
            byte[] header = inputStream.readNBytes(12);

            return switch (contentType) {
                case "image/jpeg" -> startsWith(header, 0xFF, 0xD8, 0xFF);
                case "image/png" -> startsWith(
                        header, 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A
                );
                case "image/gif" -> startsWith(header, 'G', 'I', 'F', '8', '7', 'a')
                        || startsWith(header, 'G', 'I', 'F', '8', '9', 'a');
                case "image/webp" -> startsWith(header, 'R', 'I', 'F', 'F')
                        && containsAt(header, 8, 'W', 'E', 'B', 'P');
                default -> false;
            };
        } catch (IOException exception) {
            throw new IllegalArgumentException("이미지 파일을 읽을 수 없습니다.", exception);
        }
    }

    /**
     * 바이트 배열이 지정한 값으로 시작하는지 확인한다.
     *
     * @param bytes 검사할 바이트 배열
     * @param expected 예상 바이트 값
     * @return 시작 바이트가 모두 일치하면 true
     */
    private boolean startsWith(byte[] bytes, int... expected) {
        return containsAt(bytes, 0, expected);
    }

    /**
     * 바이트 배열의 지정 위치부터 예상 값이 일치하는지 확인한다.
     *
     * @param bytes 검사할 바이트 배열
     * @param offset 검사를 시작할 위치
     * @param expected 예상 바이트 값
     * @return 지정 위치의 바이트가 모두 일치하면 true
     */
    private boolean containsAt(byte[] bytes, int offset, int... expected) {
        if (bytes.length < offset + expected.length) {
            return false;
        }

        for (int index = 0; index < expected.length; index++) {
            if (bytes[offset + index] != (byte) expected[index]) {
                return false;
            }
        }

        return true;
    }

    /**
     * 저장 실패 과정에서 생성된 불완전한 파일을 삭제한다.
     *
     * @param targetPath 삭제할 파일 경로
     * @param originalException 최초 저장 예외
     */
    private void deleteIncompleteFile(Path targetPath, IOException originalException) {
        try {
            Files.deleteIfExists(targetPath);
        } catch (IOException cleanupException) {
            originalException.addSuppressed(cleanupException);
        }
    }

    /**
     * 이미지 조회 실패를 나타내는 404 예외를 생성한다.
     *
     * @return 이미지 조회 실패 예외
     */
    private ResponseStatusException imageNotFound() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "이미지를 찾을 수 없습니다.");
    }
}
