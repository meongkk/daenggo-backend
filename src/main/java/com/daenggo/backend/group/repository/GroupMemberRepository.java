package com.daenggo.backend.group.repository;

import com.daenggo.backend.group.entity.GroupMember;
import com.daenggo.backend.group.entity.GroupMemberStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {

    Optional<GroupMember> findByGroupIdAndUserId(Long groupId, Long userId);

    boolean existsByGroupIdAndUserIdAndStatus(
            Long groupId,
            Long userId,
            GroupMemberStatus status
    );

    List<GroupMember> findAllByGroupIdAndStatus(
            Long groupId,
            GroupMemberStatus status
    );
}
