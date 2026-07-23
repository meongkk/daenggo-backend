package com.daenggo.backend.group.controller;

import com.daenggo.backend.group.dto.GroupRequestDto;
import com.daenggo.backend.group.dto.GroupResponseDto;
import com.daenggo.backend.group.service.GroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
