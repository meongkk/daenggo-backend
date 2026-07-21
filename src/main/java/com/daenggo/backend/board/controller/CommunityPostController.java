package com.daenggo.backend.board.controller;

import com.daenggo.backend.board.dto.BoardResponse;
import com.daenggo.backend.board.service.CommunityPostService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 커뮤니티 게시글 등록 요청을 처리하는 컨트롤러.
 */
@RestController
@RequestMapping("/api/community/posts")
public class CommunityPostController {

    /** 커뮤니티 게시글 등록 서비스. */
    private final CommunityPostService communityPostService;

    /**
     * 커뮤니티 게시글 컨트롤러를 생성한다.
     *
     * @param communityPostService 게시글 등록 서비스
     */
    public CommunityPostController(CommunityPostService communityPostService) {
        this.communityPostService = communityPostService;
    }

    /**
     * 새로운 커뮤니티 게시글을 등록한다.
     *
     * @param request 게시글 등록 요청 데이터
     * @return 등록된 게시글과 HTTP 201 응답
     */
    @PostMapping
    public ResponseEntity<BoardResponse> createPost(
            @Valid @RequestBody CreateCommunityPostRequest request
    ) {
        BoardResponse response = communityPostService.createPost(
                request.title(),
                request.content(),
                request.userId()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 커뮤니티 게시글 등록에 필요한 요청 데이터.
     *
     * @param title 게시글 제목
     * @param content 게시글 내용
     * @param userId 작성자 식별자
     */
    public record CreateCommunityPostRequest(
            @NotBlank(message = "게시글 제목을 입력해주세요.")
            String title,

            @NotBlank(message = "게시글 내용을 입력해주세요.")
            String content,

            @NotNull(message = "작성자 ID를 입력해주세요.")
            @Positive(message = "작성자 ID는 양수여야 합니다.")
            Long userId
    ) {
    }
}
