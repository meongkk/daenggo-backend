package com.daenggo.backend.group.controller;

import com.daenggo.backend.group.dto.GroupRequestDto;
import com.daenggo.backend.group.dto.GroupResponseDto;
import com.daenggo.backend.group.service.GroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 그룹 관리 REST 컨트롤러
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/groups")
public class GroupController {

    private final GroupService groupService;

    /**
     * 로그인 회원의 그룹 생성
     *
     * @param authentication 로그인 회원 인증 정보
     * @param request 그룹 생성 요청
     * @return 생성된 그룹 상세 정보 응답
     */
    @PostMapping
    public ResponseEntity<GroupResponseDto.Detail> create(
            final Authentication authentication,
            @Valid @RequestBody final GroupRequestDto.Create request
    ) {
        final GroupResponseDto.Detail response = groupService.create(
                authentication.getName(),
                request
        );
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    /**
     * 로그인 회원이 참여 중인 그룹 목록 조회
     *
     * @param authentication 로그인 회원 인증 정보
     * @return 참여 중인 그룹 요약 정보 목록 응답
     */
    @GetMapping
    public ResponseEntity<List<GroupResponseDto.Summary>> getMyGroups(
            final Authentication authentication
    ) {
        final List<GroupResponseDto.Summary> response = groupService.getMyGroups(
                authentication.getName()
        );
        return ResponseEntity.ok(response);
    }

    /**
     * 로그인 회원이 선택한 참여 그룹의 상세 정보 조회
     *
     * @param authentication 로그인 회원 인증 정보
     * @param groupId 조회할 그룹 ID
     * @return 선택한 그룹 상세 정보 응답
     */
    @GetMapping("/{groupId}")
    public ResponseEntity<GroupResponseDto.Detail> getGroupDetail(
            final Authentication authentication,
            @PathVariable final Long groupId
    ) {
        final GroupResponseDto.Detail response = groupService.getGroupDetail(
                authentication.getName(),
                groupId
        );
        return ResponseEntity.ok(response);
    }

    /**
     * 회원이 참여 중인 그룹의 그룹원 목록 조회
     *
     * @param authentication 로그인 회원 인증 정보
     * @param groupId 조회할 그룹 ID
     * @return 활동 중인 그룹원 정보 목록 응답
     */
    @GetMapping("/{groupId}/members")
    public ResponseEntity<List<GroupResponseDto.Member>> getGroupMembers(
            final Authentication authentication,
            @PathVariable final Long groupId
    ) {
        final List<GroupResponseDto.Member> response = groupService.getGroupMembers(
                authentication.getName(),
                groupId
        );
        return ResponseEntity.ok(response);
    }
}
