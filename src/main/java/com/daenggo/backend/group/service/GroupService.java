package com.daenggo.backend.group.service;

import com.daenggo.backend.group.dto.GroupRequestDto;
import com.daenggo.backend.group.dto.GroupResponseDto;
import com.daenggo.backend.group.entity.Group;
import com.daenggo.backend.group.entity.GroupMember;
import com.daenggo.backend.group.entity.GroupMemberRole;
import com.daenggo.backend.group.entity.GroupMemberStatus;
import com.daenggo.backend.group.repository.GroupMemberRepository;
import com.daenggo.backend.group.repository.GroupRepository;
import com.daenggo.backend.user.entity.User;
import com.daenggo.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

/**
 * 그룹 관리 비즈니스 로직
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GroupService {

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserRepository userRepository;

    /**
     * 그룹 생성 및 생성 회원의 그룹장 등록
     *
     * @param email 로그인 회원 이메일
     * @param request 그룹 생성 요청
     * @return 생성된 그룹 상세 정보
     */
    @Transactional
    public GroupResponseDto.Detail create(
            final String email,
            final GroupRequestDto.Create request
    ) {
        final User owner = findActiveUser(email);

        final Group group = groupRepository.save(Group.builder()
                .owner(owner)
                .name(request.getName().strip())
                .description(normalizeDescription(request.getDescription()))
                .build());

        groupMemberRepository.save(GroupMember.builder()
                .group(group)
                .user(owner)
                .role(GroupMemberRole.OWNER)
                .status(GroupMemberStatus.ACTIVE)
                .build());

        return GroupResponseDto.Detail.from(
                group,
                1L,
                GroupMemberRole.OWNER
        );
    }

    /**
     * 로그인한 회원이 참여 중인 그룹 목록 조회
     *
     * @param email 로그인 회원 이메일
     * @return 참여 중인 그룹 요약 정보 목록
     */
    public List<GroupResponseDto.Summary> getMyGroups(final String email) {
        final User user = findActiveUser(email);

        return groupMemberRepository.findAllByUserIdAndStatusOrderByJoinedAtDesc(
                        user.getId(),
                        GroupMemberStatus.ACTIVE
                )
                .stream()
                .map(member -> GroupResponseDto.Summary.from(
                        member.getGroup(),
                        groupMemberRepository.countByGroupIdAndStatus(
                                member.getGroup().getId(),
                                GroupMemberStatus.ACTIVE
                        ),
                        member.getRole()
                ))
                .toList();
    }

    /**
     * 탈퇴하지 않은 로그인 회원 조회
     *
     * @param email 로그인 회원 이메일
     * @return 활성 회원 엔티티
     */
    private User findActiveUser(final String email) {
        return userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "인증된 회원을 찾을 수 없습니다."
                ));
    }

    /**
     * 그룹 설명의 앞뒤 공백 제거 및 빈 값 정규화
     *
     * @param description 입력된 그룹 설명
     * @return 앞뒤 공백이 제거된 그룹 설명 또는 빈 값인 경우 null
     */
    private String normalizeDescription(final String description) {
        if (description == null || description.isBlank()) {
            return null;
        }
        return description.strip();
    }
}
