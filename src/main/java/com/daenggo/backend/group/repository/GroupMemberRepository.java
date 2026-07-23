package com.daenggo.backend.group.repository;

import com.daenggo.backend.group.entity.GroupMember;
import com.daenggo.backend.group.entity.GroupMemberStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {

    @EntityGraph(attributePaths = {"group", "group.owner"})
    Optional<GroupMember> findByGroupIdAndUserIdAndStatus(
            Long groupId,
            Long userId,
            GroupMemberStatus status
    );

    @EntityGraph(attributePaths = "user")
    Optional<GroupMember> findByIdAndGroupIdAndStatus(
            Long memberId,
            Long groupId,
            GroupMemberStatus status
    );

    boolean existsByGroupIdAndUserIdAndStatus(
            Long groupId,
            Long userId,
            GroupMemberStatus status
    );

    @EntityGraph(attributePaths = "user")
    List<GroupMember> findAllByGroupIdAndStatusOrderByJoinedAtAsc(
            Long groupId,
            GroupMemberStatus status
    );

    @EntityGraph(attributePaths = "group")
    List<GroupMember> findAllByUserIdAndStatusOrderByJoinedAtDesc(
            Long userId,
            GroupMemberStatus status
    );

    long countByGroupIdAndStatus(Long groupId, GroupMemberStatus status);
}
