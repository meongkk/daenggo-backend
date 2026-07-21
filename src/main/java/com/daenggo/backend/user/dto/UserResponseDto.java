package com.daenggo.backend.user.dto;

import com.daenggo.backend.user.entity.AuthProvider;
import com.daenggo.backend.user.entity.User;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UserResponseDto {

    @Getter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Me {

        private final Long id;
        private final String email;
        private final String nickname;
        private final String profileImageUrl;
        private final AuthProvider loginType;

        public static Me from(final User user) {
            return new Me(
                    user.getId(),
                    user.getEmail(),
                    user.getNickname(),
                    user.getImage(),
                    user.getProvider()
            );
        }
    }
}
