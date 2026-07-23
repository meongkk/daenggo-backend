package com.daenggo.backend.board.service;
import java.util.Objects;
import com.daenggo.backend.board.dto.BoardResponse;
import com.daenggo.backend.board.dto.CommunityCategory;
import com.daenggo.backend.board.entity.Board;
import com.daenggo.backend.board.entity.BoardImage;
import com.daenggo.backend.board.entity.MarketBoard;
import com.daenggo.backend.board.repository.BoardImageRepository;
import com.daenggo.backend.board.repository.BoardRepository;
import com.daenggo.backend.board.repository.CommentRepository;
import com.daenggo.backend.board.repository.MarketBoardRepository;
import com.daenggo.backend.user.entity.User;
import jakarta.persistence.EntityManager;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 커뮤니티 게시글 등록과 목록 조회 비즈니스 로직을 처리하는 서비스.
 */
@Service
public class CommunityPostService {

    /** 게시글 하나에 연결할 수 있는 최대 이미지 개수. */
    private static final int MAX_IMAGE_COUNT = 5;

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

    /** 장터 글의 가격과 삽니다/팝니다 상태를 저장하고 조회하는 저장소. */
    private final MarketBoardRepository marketBoardRepository;

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
            MarketBoardRepository marketBoardRepository,
            EntityManager entityManager
    ) {
        this.boardRepository = boardRepository;
        this.boardImageRepository = boardImageRepository;
        this.commentRepository = commentRepository;
        this.marketBoardRepository = marketBoardRepository;
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

        // 장터 게시판일 때만 장터 전용 테이블의 가격과 거래 상태를 한 번에 조회합니다.
        Map<Long, MarketBoard> marketBoardByBoardId = category == CommunityCategory.MARKET
                ? marketBoardRepository.findAllById(boardIds).stream()
                        .collect(Collectors.toMap(MarketBoard::getId, marketBoard -> marketBoard))
                : Map.of();

        return boards.stream()
                .map(board -> BoardResponse.from(
                        board,
                        imageUrlsByBoardId.getOrDefault(board.getId(), List.of()),
                        commentCountByBoardId.getOrDefault(board.getId(), 0L),
                        marketBoardByBoardId.get(board.getId())
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
        MarketBoard marketBoard = marketBoardRepository.findById(postId).orElse(null);

        return BoardResponse.from(board, imageUrls, commentCount, marketBoard);
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
            List<String> imageUrls,
            Integer price,
            String tradeStatus
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
        MarketBoard savedMarketBoard = null;

        if (category == CommunityCategory.MARKET) {
            String normalizedTradeStatus = tradeStatus == null
                    ? null
                    : tradeStatus.trim().toUpperCase(Locale.ROOT);

            if (price == null || price < 0) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "장터 게시글의 가격은 0원 이상이어야 합니다."
                );
            }

            if (!Set.of("SELL", "BUY").contains(normalizedTradeStatus)) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "거래 종류는 SELL(팝니다) 또는 BUY(삽니다)여야 합니다."
                );
            }

            // board의 post_id를 그대로 사용해 market_board에 장터 정보를 연결합니다.
            savedMarketBoard = marketBoardRepository.save(
                    new MarketBoard(savedBoard, price, normalizedTradeStatus)
            );
        }

        if (!validatedImageUrls.isEmpty()) {
            List<BoardImage> boardImages = validatedImageUrls.stream()
                    .map(imageUrl -> new BoardImage(savedBoard, imageUrl))
                    .toList();
            boardImageRepository.saveAll(boardImages);
        }

        return BoardResponse.from(savedBoard, validatedImageUrls, 0L, savedMarketBoard);
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

        if (imageUrls.size() > MAX_IMAGE_COUNT) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "이미지는 최대 5장까지 등록할 수 있습니다."
            );
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
    /**
     * 게시글 작성자를 확인한 후 게시글을 소프트 삭제한다.
     *
     * @param postId 삭제할 게시글 식별자
     * @param userId 삭제를 요청한 사용자 식별자
     * @throws ResponseStatusException 게시글이 없거나 이미 삭제된 경우
     * @throws ResponseStatusException 요청한 사용자가 작성자가 아닌 경우
     */
    @Transactional
    public void deletePost(Long postId, Long userId) {
        Board board = boardRepository.findById(postId)
                .filter(item -> item.getDeletedAt() == null)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "게시글을 찾을 수 없습니다. postId=" + postId
                ));

        if (!Objects.equals(board.getUser().getId(), userId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "게시글 작성자만 삭제할 수 있습니다."
            );
        }

        board.deleteSoftly();
    }

    /**
     * 게시글 작성자를 확인한 후 제목과 내용을 수정한다.
     *
     * @param postId 수정할 게시글 식별자
     * @param userId 수정을 요청한 사용자 식별자
     * @param title 변경할 게시글 제목
     * @param content 변경할 게시글 내용
     * @param imageUrls 수정 후 게시글에 남길 이미지 URL 목록, null이면 기존 이미지 유지
     * @return 수정된 게시글 응답 데이터
     * @throws ResponseStatusException 게시글이 없거나 이미 삭제된 경우
     * @throws ResponseStatusException 요청한 사용자가 작성자가 아닌 경우
     */
    @Transactional
    public BoardResponse updatePost(
            Long postId,
            Long userId,
            String title,
            String content,
            List<String> imageUrls
    ) {
        Board board = boardRepository.findActiveById(postId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "게시글을 찾을 수 없습니다. postId=" + postId
                ));

        if (!Objects.equals(board.getUser().getId(), userId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "게시글 작성자만 수정할 수 있습니다."
            );
        }

        board.update(title.trim(), content.trim());

        List<BoardImage> currentBoardImages = boardImageRepository
                .findAllByBoard_IdInOrderByIdAsc(List.of(postId));
        List<String> responseImageUrls = currentBoardImages.stream()
                .map(BoardImage::getImageUrl)
                .toList();

        if (imageUrls != null) {
            List<String> validatedImageUrls = validateImageUrls(imageUrls);
            boardImageRepository.deleteAll(currentBoardImages);

            if (!validatedImageUrls.isEmpty()) {
                List<BoardImage> updatedBoardImages = validatedImageUrls.stream()
                        .map(imageUrl -> new BoardImage(board, imageUrl))
                        .toList();
                boardImageRepository.saveAll(updatedBoardImages);
            }

            responseImageUrls = validatedImageUrls;
        }

        long commentCount = commentRepository.countByBoard_Id(postId);
        MarketBoard marketBoard = marketBoardRepository.findById(postId).orElse(null);

        return BoardResponse.from(board, responseImageUrls, commentCount, marketBoard);
    }

}
