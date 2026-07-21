package com.daenggo.backend.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UserRequestDto {

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class Update {

        @Size(max = 50)
        private String nickname;

        @Size(max = 250)
        private String profileImageUrl;

        @NotBlank
        private String currentPassword;
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class PasswordChange {

        @NotBlank
        private String currentPassword;

        @NotBlank
        private String newPassword;
    }
}
