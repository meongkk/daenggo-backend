package com.daenggo.backend.board.controller;

import com.daenggo.backend.board.dto.CommentRequest;
import com.daenggo.backend.board.dto.CommentResponse;
import com.daenggo.backend.board.service.CommunityCommentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 커뮤니티 게시글의 댓글 등록과 목록 조회 요청을 처리하는 컨트롤러.
 */
@RestController
@RequestMapping("/api/community/posts/{postId}/comments")
public class CommunityCommentController {

    /** 커뮤니티 댓글 등록과 목록 조회 서비스. */
    private final CommunityCommentService communityCommentService;

    /**
     * 커뮤니티 댓글 컨트롤러를 생성한다.
     *
     * @param communityCommentService 커뮤니티 댓글 서비스
     */
    public CommunityCommentController(CommunityCommentService communityCommentService) {
        this.communityCommentService = communityCommentService;
    }

    /**
     * 게시글에 등록된 댓글을 오래된 순서부터 조회한다.
     *
     * @param postId 댓글을 조회할 게시글 식별자
     * @return 게시글 댓글 목록
     */
    @GetMapping
    public ResponseEntity<List<CommentResponse>> getComments(@PathVariable Long postId) {
        return ResponseEntity.ok(communityCommentService.getComments(postId));
    }

    /**
     * 게시글에 새로운 댓글을 등록한다.
     *
     * @param postId 댓글을 등록할 게시글 식별자
     * @param request 댓글 내용과 작성자 식별자
     * @return 등록된 댓글과 HTTP 201 응답
     */
    @PostMapping
    public ResponseEntity<CommentResponse> createComment(
            @PathVariable Long postId,
            @Valid @RequestBody CommentRequest request
    ) {
        CommentResponse response = communityCommentService.createComment(
                postId,
                request.getContent(),
                request.getUserId()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /** 작성자 본인의 댓글 내용을 수정한다. */
    @PatchMapping("/{commentId}")
    public ResponseEntity<CommentResponse> updateComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @Valid @RequestBody CommentRequest request
    ) {
        CommentResponse response = communityCommentService.updateComment(
                postId,
                commentId,
                request.getContent(),
                request.getUserId()
        );

        return ResponseEntity.ok(response);
    }

    /** 작성자 본인의 댓글을 삭제한다. */
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @RequestParam Long userId
    ) {
        communityCommentService.deleteComment(postId, commentId, userId);
        return ResponseEntity.noContent().build();
    }
}
