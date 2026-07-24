package com.daenggo.backend.board.controller;

import com.daenggo.backend.board.dto.CommentRequest;
import com.daenggo.backend.board.dto.CommentResponse;
import com.daenggo.backend.board.service.CommunityCommentService;
import jakarta.validation.Valid;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 커뮤니티 댓글의 등록, 조회, 수정, 삭제 HTTP 요청을 받는다. */
@RestController
@RequestMapping("/api/community/posts/{postId}/comments")
public class CommunityCommentController {

    private final CommunityCommentService communityCommentService;

    public CommunityCommentController(CommunityCommentService communityCommentService) {
        this.communityCommentService = communityCommentService;
    }

    /** 게시글의 댓글 목록을 반환한다. */
    @GetMapping
    public ResponseEntity<List<CommentResponse>> getComments(@PathVariable Long postId) {
        return ResponseEntity.ok(communityCommentService.getComments(postId));
    }

    /** JWT 로그인 사용자를 작성자로 하여 댓글을 등록한다. */
    @PostMapping
    public ResponseEntity<CommentResponse> createComment(
            @PathVariable Long postId,
            @Valid @RequestBody CommentRequest request,
            Authentication authentication
    ) {
        CommentResponse response = communityCommentService.createComment(
                postId, request.getContent(), authentication.getName()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /** JWT 로그인 사용자가 작성한 댓글만 수정한다. */
    @PatchMapping("/{commentId}")
    public ResponseEntity<CommentResponse> updateComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @Valid @RequestBody CommentRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(communityCommentService.updateComment(
                postId, commentId, request.getContent(), authentication.getName()
        ));
    }

    /** JWT 로그인 사용자가 작성한 댓글만 삭제한다. */
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            Authentication authentication
    ) {
        communityCommentService.deleteComment(postId, commentId, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
