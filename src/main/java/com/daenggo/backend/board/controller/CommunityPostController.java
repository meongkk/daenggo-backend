package com.daenggo.backend.board.controller;

import com.daenggo.backend.board.dto.BoardDeleteRequest;
import com.daenggo.backend.board.dto.BoardResponse;
import com.daenggo.backend.board.dto.BoardUpdateRequest;
import com.daenggo.backend.board.dto.CommunityCategory;
import com.daenggo.backend.board.service.CommunityPostService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 커뮤니티 게시글 등록과 목록 조회 요청을 처리하는 컨트롤러.
 */
@RestController
@RequestMapping("/api/community/posts")
public class CommunityPostController {

    /** 커뮤니티 게시글 등록과 목록 조회 서비스. */
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
     * 요청한 카테고리에 속한 게시글 목록을 최신순으로 조회한다.
     *
     * @param category 조회할 커뮤니티 카테고리
     * @return 게시글과 게시글별 이미지 URL 목록
     */
    @GetMapping
    public ResponseEntity<List<BoardResponse>> getPosts(
            @RequestParam CommunityCategory category
    ) {
        return ResponseEntity.ok(communityPostService.getPosts(category));
    }

    /**
     * 게시글 상세를 조회하고 조회수를 1 증가시킨다.
     *
     * @param postId 조회할 게시글 식별자
     * @return 조회수가 증가된 게시글 상세
     */
    @GetMapping("/{postId}")
    public ResponseEntity<BoardResponse> getPost(@PathVariable Long postId) {
        return ResponseEntity.ok(communityPostService.getPost(postId));
    }

    /**
     * 작성자 본인의 커뮤니티 게시글 제목과 내용을 수정한다.
     *
     * @param postId 수정할 게시글 식별자
     * @param request 사용자 ID와 변경할 제목·내용
     * @return 수정된 게시글 데이터와 HTTP 200 응답
     */
    @PatchMapping("/{postId}")
    public ResponseEntity<BoardResponse> updatePost(
            @PathVariable Long postId,
            @Valid @RequestBody BoardUpdateRequest request
    ) {
        BoardResponse response = communityPostService.updatePost(
                postId,
                request.getUserId(),
                request.getTitle(),
                request.getContent(),
                request.getImageUrls()
        );

        return ResponseEntity.ok(response);
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
                request.category(),
                request.title(),
                request.content(),
                request.userId(),
                request.imageUrls(),
                request.price(),
                request.tradeStatus()
        );
        System.out.println("프론트에서 넘어온 유저 ID: " + request.userId());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    /**
     * 작성자 본인의 커뮤니티 게시글을 삭제한다.
     *
     * @param postId 삭제할 게시글 식별자
     * @param request 삭제를 요청한 사용자 정보
     * @return 응답 본문이 없는 HTTP 204 응답
     */

    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(
            @PathVariable Long postId,
            @Valid @RequestBody BoardDeleteRequest request
    ) {
        communityPostService.deletePost(postId, request.userId());

        return ResponseEntity.noContent().build();
    }

    /**
     * 커뮤니티 게시글 등록에 필요한 요청 데이터.
     *
     * @param category 게시글을 등록할 커뮤니티 카테고리
     * @param title 게시글 제목
     * @param content 게시글 내용
     * @param userId 작성자 식별자
     * @param imageUrls 업로드가 끝난 이미지 조회 URL 목록
     */
    public record CreateCommunityPostRequest(
            @NotNull(message = "게시글 카테고리를 선택해주세요.")
            CommunityCategory category,

            @NotBlank(message = "게시글 제목을 입력해주세요.")
            String title,

            @NotBlank(message = "게시글 내용을 입력해주세요.")
            String content,

            @NotNull(message = "작성자 ID를 입력해주세요.")
            @Positive(message = "작성자 ID는 양수여야 합니다.")
            Long userId,

            @Size(max = 5, message = "이미지는 최대 5장까지 등록할 수 있습니다.")
            List<@NotBlank(message = "이미지 주소가 비어 있습니다.") String> imageUrls,

            Integer price,

            String tradeStatus
    ) {

    }


}
