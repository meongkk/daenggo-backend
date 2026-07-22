package com.daenggo.backend.board.service;

import com.daenggo.backend.board.dto.BoardResponse;
import com.daenggo.backend.board.dto.CommunityCategory;
import com.daenggo.backend.board.entity.Board;
import com.daenggo.backend.board.entity.BoardImage;
import com.daenggo.backend.board.repository.BoardImageRepository;
import com.daenggo.backend.board.repository.BoardRepository;
import com.daenggo.backend.board.repository.CommentRepository;
import com.daenggo.backend.user.entity.User;
import jakarta.persistence.EntityManager;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 커뮤니티 게시글 등록과 목록 조회 비즈니스 로직을 처리하는 서비스.
 */
@Service
public class CommunityPostService {

    /** 이미지 업로드 API가 발급한 안전한 URL 형식. */
    private static final Pattern COMMUNITY_IMAGE_URL_PATTERN = Pattern.compile(
            "^/api/community/images/[0-9a-fA-F-]{36}\\.(jpg|png|gif|webp)$"
    );

    /** 게시글 저장소. */
    private final BoardRepository boardRepository;

    /** 게시글과 업로드 이미지를 연결해 저장하는 저장소. */
    private final BoardImageRepository boardImageRepository;

    /** 게시글별 실제 댓글 개수를 조회하는 저장소. */
    private final CommentRepository commentRepository;

    /** 작성자 엔티티 조회에 사용하는 엔티티 매니저. */
    private final EntityManager entityManager;

    /**
     * 커뮤니티 게시글 서비스를 생성한다.
     *
     * @param boardRepository 게시글 저장소
     * @param boardImageRepository 게시글 이미지 저장소
     * @param entityManager 작성자 조회에 사용할 엔티티 매니저
     */
    public CommunityPostService(
            BoardRepository boardRepository,
            BoardImageRepository boardImageRepository,
            CommentRepository commentRepository,
            EntityManager entityManager
    ) {
        this.boardRepository = boardRepository;
        this.boardImageRepository = boardImageRepository;
        this.commentRepository = commentRepository;
        this.entityManager = entityManager;
    }

    /**
     * 카테고리가 일치하고 삭제되지 않은 게시글을 최신순으로 조회한다.
     * 게시글 이미지도 함께 조회해 각 게시글 응답에 연결한다.
     *
     * @param category 조회할 커뮤니티 카테고리
     * @return 카테고리에 속한 게시글 응답 목록
     */
    @Transactional(readOnly = true)
    public List<BoardResponse> getPosts(CommunityCategory category) {
        List<Board> boards = boardRepository
                .findAllByTypeAndDeletedAtIsNullOrderByCreatedAtDesc(category.name());

        if (boards.isEmpty()) {
            return List.of();
        }

        List<Long> boardIds = boards.stream()
                .map(Board::getId)
                .toList();

        Map<Long, List<String>> imageUrlsByBoardId = boardImageRepository
                .findAllByBoard_IdInOrderByIdAsc(boardIds)
                .stream()
                .collect(Collectors.groupingBy(
                        boardImage -> boardImage.getBoard().getId(),
                        Collectors.mapping(BoardImage::getImageUrl, Collectors.toList())
                ));

        Map<Long, Long> commentCountByBoardId = commentRepository
                .countAllByBoardIds(boardIds)
                .stream()
                .collect(Collectors.toMap(
                        CommentRepository.BoardCommentCount::getBoardId,
                        CommentRepository.BoardCommentCount::getCommentCount
                ));

        return boards.stream()
                .map(board -> BoardResponse.from(
                        board,
                        imageUrlsByBoardId.getOrDefault(board.getId(), List.of()),
                        commentCountByBoardId.getOrDefault(board.getId(), 0L)
                ))
                .toList();
    }

    /**
     * 삭제되지 않은 게시글 상세를 조회하고 조회수를 1 증가시킨다.
     * 트랜잭션 안에서 엔티티 값을 변경하므로 변경 감지에 의해 DB에 반영된다.
     *
     * @param postId 조회할 게시글 식별자
     * @return 조회수가 증가된 게시글 상세 응답
     * @throws ResponseStatusException 게시글이 없거나 삭제된 경우
     */
    @Transactional
    public BoardResponse getPost(Long postId) {
        Board board = boardRepository.findById(postId)
                .filter(item -> item.getDeletedAt() == null)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "게시글을 찾을 수 없습니다. postId=" + postId
                ));

        board.increaseViewCount();

        List<String> imageUrls = boardImageRepository
                .findAllByBoard_IdInOrderByIdAsc(List.of(postId))
                .stream()
                .map(BoardImage::getImageUrl)
                .toList();

        long commentCount = commentRepository.countByBoard_Id(postId);

        return BoardResponse.from(board, imageUrls, commentCount);
    }

    /**
     * 작성자를 확인하고 새로운 커뮤니티 게시글을 저장한다.
     *
     * @param category 게시글을 등록할 커뮤니티 카테고리
     * @param title 게시글 제목
     * @param content 게시글 내용
     * @param userId 작성자 식별자
     * @param imageUrls 업로드가 끝난 이미지 조회 URL 목록
     * @return 저장된 게시글 응답 데이터
     * @throws ResponseStatusException 작성자를 찾을 수 없는 경우
     */
    @Transactional
    public BoardResponse createPost(
            CommunityCategory category,
            String title,
            String content,
            Long userId,
            List<String> imageUrls
    ) {
        User user = entityManager.find(User.class, userId);

        if (user == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "작성자를 찾을 수 없습니다. userId=" + userId
            );
        }

        List<String> validatedImageUrls = validateImageUrls(imageUrls);
        Board board = new Board(category.name(), title, content, user);
        Board savedBoard = boardRepository.save(board);

        if (!validatedImageUrls.isEmpty()) {
            List<BoardImage> boardImages = validatedImageUrls.stream()
                    .map(imageUrl -> new BoardImage(savedBoard, imageUrl))
                    .toList();
            boardImageRepository.saveAll(boardImages);
        }

        return BoardResponse.from(savedBoard, validatedImageUrls);
    }

    /**
     * 이미지 업로드 API가 발급한 주소만 게시글에 연결되도록 검사한다.
     *
     * @param imageUrls 검사할 이미지 URL 목록
     * @return 공백을 제거하고 검증한 이미지 URL 목록
     */
    private List<String> validateImageUrls(List<String> imageUrls) {
        if (imageUrls == null) {
            return List.of();
        }

        List<String> normalizedImageUrls = imageUrls.stream()
                .map(String::trim)
                .toList();

        boolean hasInvalidUrl = normalizedImageUrls.stream()
                .anyMatch(imageUrl -> !COMMUNITY_IMAGE_URL_PATTERN.matcher(imageUrl).matches());

        if (hasInvalidUrl) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "올바르지 않은 커뮤니티 이미지 주소가 포함되어 있습니다."
            );
        }

        return normalizedImageUrls;
    }
}
