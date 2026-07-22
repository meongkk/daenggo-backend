package com.daenggo.backend.group.dto;

import com.daenggo.backend.group.entity.Group;
import com.daenggo.backend.group.entity.GroupMember;
import com.daenggo.backend.group.entity.GroupMemberRole;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class GroupResponseDto {

    @Getter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Summary {

        private final Long groupId;
        private final String name;
        private final String description;
        private final long memberCount;
        private final GroupMemberRole myRole;

        public static Summary from(
                final Group group,
                final long memberCount,
                final GroupMemberRole myRole
        ) {
            return new Summary(
                    group.getId(),
                    group.getName(),
                    group.getDescription(),
                    memberCount,
                    myRole
            );
        }
    }

    @Getter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Detail {

        private final Long groupId;
        private final String name;
        private final String description;
        private final Long ownerId;
        private final String ownerNickname;
        private final long memberCount;
        private final GroupMemberRole myRole;
        private final LocalDateTime createdAt;

        public static Detail from(
                final Group group,
                final long memberCount,
                final GroupMemberRole myRole
        ) {
            return new Detail(
                    group.getId(),
                    group.getName(),
                    group.getDescription(),
                    group.getOwner().getId(),
                    group.getOwner().getNickname(),
                    memberCount,
                    myRole,
                    group.getCreatedAt()
            );
        }
    }

    @Getter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Member {

        private final Long memberId;
        private final Long userId;
        private final String nickname;
        private final String profileImageUrl;
        private final GroupMemberRole role;
        private final LocalDateTime joinedAt;

        public static Member from(final GroupMember member) {
            return new Member(
                    member.getId(),
                    member.getUser().getId(),
                    member.getUser().getNickname(),
                    member.getUser().getImage(),
                    member.getRole(),
                    member.getJoinedAt()
            );
        }
    }
}
