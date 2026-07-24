package com.daenggo.backend.profile.service;

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
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * 유저와 펫의 프로필 이미지 파일을 검증하고 로컬 저장소에 저장한다.
 */
@Service
public class ProfileImageService {

    private static final Map<String, String> IMAGE_EXTENSIONS = Map.of(
            "image/jpeg", ".jpg",
            "image/png", ".png",
            "image/gif", ".gif",
            "image/webp", ".webp"
    );

    private static final Pattern SAFE_FILE_NAME = Pattern.compile(
            "^[0-9a-fA-F-]{36}\\.(jpg|png|gif|webp)$"
    );

    private final Path uploadRootDirectory;

    public ProfileImageService(
            @Value("${app.profile-image.upload-directory:uploads}") final String uploadDirectory
    ) {
        this.uploadRootDirectory = Path.of(uploadDirectory).toAbsolutePath().normalize();
    }

    public String uploadUserImage(final MultipartFile image) {
        return upload(image, ProfileImageType.USER);
    }

    public String uploadPetImage(final MultipartFile image) {
        return upload(image, ProfileImageType.PET);
    }

    public Resource loadUserImage(final String fileName) {
        return load(fileName, ProfileImageType.USER);
    }

    public Resource loadPetImage(final String fileName) {
        return load(fileName, ProfileImageType.PET);
    }

    private String upload(final MultipartFile image, final ProfileImageType imageType) {
        final String extension = validateAndGetExtension(image);
        final String fileName = UUID.randomUUID() + extension;
        final Path uploadDirectory = resolveUploadDirectory(imageType);
        final Path targetPath = uploadDirectory.resolve(fileName).normalize();

        if (!targetPath.startsWith(uploadDirectory)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "올바르지 않은 이미지 저장 경로입니다."
            );
        }

        try {
            Files.createDirectories(uploadDirectory);

            try (InputStream inputStream = image.getInputStream()) {
                Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException exception) {
            deleteIncompleteFile(targetPath, exception);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "이미지 저장 중 오류가 발생했습니다.",
                    exception
            );
        }

        return imageType.apiPath + fileName;
    }

    private Resource load(final String fileName, final ProfileImageType imageType) {
        if (fileName == null || !SAFE_FILE_NAME.matcher(fileName).matches()) {
            throw imageNotFound();
        }

        final Path uploadDirectory = resolveUploadDirectory(imageType);
        final Path imagePath = uploadDirectory.resolve(fileName).normalize();

        if (!imagePath.startsWith(uploadDirectory) || !Files.isRegularFile(imagePath)) {
            throw imageNotFound();
        }

        return new FileSystemResource(imagePath);
    }

    private Path resolveUploadDirectory(final ProfileImageType imageType) {
        return uploadRootDirectory.resolve(imageType.directoryName).normalize();
    }

    private String validateAndGetExtension(final MultipartFile image) {
        if (image == null || image.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "이미지 파일을 선택해 주세요."
            );
        }

        final String contentType = image.getContentType();
        final String normalizedContentType = contentType == null
                ? ""
                : contentType.toLowerCase(Locale.ROOT);
        final String extension = IMAGE_EXTENSIONS.get(normalizedContentType);

        if (extension == null || !hasValidSignature(image, normalizedContentType)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "JPG, PNG, GIF, WEBP 형식의 이미지만 업로드할 수 있습니다."
            );
        }

        return extension;
    }

    private boolean hasValidSignature(
            final MultipartFile image,
            final String contentType
    ) {
        try (InputStream inputStream = image.getInputStream()) {
            final byte[] header = inputStream.readNBytes(12);

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
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "이미지 파일을 읽을 수 없습니다.",
                    exception
            );
        }
    }

    private boolean startsWith(final byte[] bytes, final int... expected) {
        return containsAt(bytes, 0, expected);
    }

    private boolean containsAt(
            final byte[] bytes,
            final int offset,
            final int... expected
    ) {
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

    private void deleteIncompleteFile(
            final Path targetPath,
            final IOException originalException
    ) {
        try {
            Files.deleteIfExists(targetPath);
        } catch (IOException cleanupException) {
            originalException.addSuppressed(cleanupException);
        }
    }

    private ResponseStatusException imageNotFound() {
        return new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "이미지를 찾을 수 없습니다."
        );
    }

    private enum ProfileImageType {
        USER("users", "/api/users/images/"),
        PET("pets", "/api/pets/images/");

        private final String directoryName;
        private final String apiPath;

        ProfileImageType(final String directoryName, final String apiPath) {
            this.directoryName = directoryName;
            this.apiPath = apiPath;
        }
    }
}
