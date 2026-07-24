package com.daenggo.backend.board.controller;

import com.daenggo.backend.board.dto.BoardResponse;
import com.daenggo.backend.board.dto.BoardUpdateRequest;
import com.daenggo.backend.board.dto.CommunityCategory;
import com.daenggo.backend.board.service.CommunityPostService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 커뮤니티 게시글의 등록, 조회, 수정, 삭제 HTTP 요청을 받는다. */
@RestController
@RequestMapping("/api/community/posts")
public class CommunityPostController {

    private final CommunityPostService communityPostService;

    public CommunityPostController(CommunityPostService communityPostService) {
        this.communityPostService = communityPostService;
    }

    /** 카테고리에 맞는 게시글 목록을 최신순으로 반환한다. */
    @GetMapping
    public ResponseEntity<List<BoardResponse>> getPosts(@RequestParam CommunityCategory category) {
        return ResponseEntity.ok(communityPostService.getPosts(category));
    }

    /** 게시글 상세를 반환하고 조회 수를 하나 올린다. */
    @GetMapping("/{postId}")
    public ResponseEntity<BoardResponse> getPost(@PathVariable Long postId) {
        return ResponseEntity.ok(communityPostService.getPost(postId));
    }

    /** JWT에 담긴 로그인 사용자만 자신의 게시글을 수정할 수 있다. */
    @PatchMapping("/{postId}")
    public ResponseEntity<BoardResponse> updatePost(
            @PathVariable Long postId,
            @Valid @RequestBody BoardUpdateRequest request,
            Authentication authentication
    ) {
        BoardResponse response = communityPostService.updatePost(
                postId,
                authentication.getName(),
                request.getTitle(),
                request.getContent(),
                request.getImageUrls()
        );
        return ResponseEntity.ok(response);
    }

    /** JWT에 담긴 로그인 사용자를 작성자로 하여 게시글을 등록한다. */
    @PostMapping
    public ResponseEntity<BoardResponse> createPost(
            @Valid @RequestBody CreateCommunityPostRequest request,
            Authentication authentication
    ) {
        BoardResponse response = communityPostService.createPost(
                request.category(),
                request.title(),
                request.content(),
                authentication.getName(),
                request.imageUrls(),
                request.price(),
                request.tradeStatus()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /** JWT에 담긴 로그인 사용자가 작성자인 경우에만 게시글을 삭제한다. */
    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(@PathVariable Long postId, Authentication authentication) {
        communityPostService.deletePost(postId, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    /** 게시글 등록 요청 데이터. userId는 받지 않고 JWT에서 작성자를 찾는다. */
    public record CreateCommunityPostRequest(
            @NotNull(message = "게시글 카테고리를 선택해주세요.") CommunityCategory category,
            @NotBlank(message = "게시글 제목을 입력해주세요.") String title,
            @NotBlank(message = "게시글 내용을 입력해주세요.") String content,
            @Size(max = 5, message = "이미지는 최대 5장까지 등록할 수 있습니다.")
            List<@NotBlank(message = "이미지 주소가 비어 있습니다.") String> imageUrls,
            Integer price,
            String tradeStatus
    ) {
    }
}
