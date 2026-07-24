package com.daenggo.backend.board.service;

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
import com.daenggo.backend.user.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/** 커뮤니티 게시글의 저장, 조회, 수정, 삭제 규칙을 처리한다. */
@Service
public class CommunityPostService {

    private static final int MAX_IMAGE_COUNT = 5;
    private static final Pattern COMMUNITY_IMAGE_URL_PATTERN = Pattern.compile(
            "^/api/community/images/[0-9a-fA-F-]{36}\\.(jpg|png|gif|webp)$"
    );

    private final BoardRepository boardRepository;
    private final BoardImageRepository boardImageRepository;
    private final CommentRepository commentRepository;
    private final MarketBoardRepository marketBoardRepository;
    private final UserRepository userRepository;

    public CommunityPostService(
            BoardRepository boardRepository,
            BoardImageRepository boardImageRepository,
            CommentRepository commentRepository,
            MarketBoardRepository marketBoardRepository,
            UserRepository userRepository
    ) {
        this.boardRepository = boardRepository;
        this.boardImageRepository = boardImageRepository;
        this.commentRepository = commentRepository;
        this.marketBoardRepository = marketBoardRepository;
        this.userRepository = userRepository;
    }

    /** 선택한 카테고리의 삭제되지 않은 게시글을 최신순으로 조회한다. */
    @Transactional(readOnly = true)
    public List<BoardResponse> getPosts(CommunityCategory category) {
        List<Board> boards = boardRepository
                .findAllByTypeAndDeletedAtIsNullOrderByCreatedAtDesc(category.name());
        if (boards.isEmpty()) {
            return List.of();
        }

        List<Long> boardIds = boards.stream().map(Board::getId).toList();
        Map<Long, List<String>> imageUrlsByBoardId = boardImageRepository
                .findAllByBoard_IdInOrderByIdAsc(boardIds)
                .stream()
                .collect(Collectors.groupingBy(
                        image -> image.getBoard().getId(),
                        Collectors.mapping(BoardImage::getImageUrl, Collectors.toList())
                ));
        Map<Long, Long> commentCountByBoardId = commentRepository.countAllByBoardIds(boardIds)
                .stream()
                .collect(Collectors.toMap(
                        CommentRepository.BoardCommentCount::getBoardId,
                        CommentRepository.BoardCommentCount::getCommentCount
                ));
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

    /** 게시글 상세를 조회하고 조회 수를 하나 증가시킨다. */
    @Transactional
    public BoardResponse getPost(Long postId) {
        Board board = findActiveBoard(postId);
        board.increaseViewCount();
        return toResponse(board);
    }

    /** JWT에서 확인한 로그인 사용자를 작성자로 하여 게시글을 저장한다. */
    @Transactional
    public BoardResponse createPost(
            CommunityCategory category,
            String title,
            String content,
            String userEmail,
            List<String> imageUrls,
            Integer price,
            String tradeStatus
    ) {
        User user = findAuthenticatedUser(userEmail);
        List<String> validatedImageUrls = validateImageUrls(imageUrls);
        Board savedBoard = boardRepository.save(new Board(category.name(), title, content, user));
        MarketBoard savedMarketBoard = saveMarketBoardIfNeeded(
                category, savedBoard, price, tradeStatus
        );

        if (!validatedImageUrls.isEmpty()) {
            boardImageRepository.saveAll(validatedImageUrls.stream()
                    .map(imageUrl -> new BoardImage(savedBoard, imageUrl))
                    .toList());
        }
        return BoardResponse.from(savedBoard, validatedImageUrls, 0L, savedMarketBoard);
    }

    /** JWT에서 확인한 로그인 사용자가 작성자인 경우에만 게시글을 소프트 삭제한다. */
    @Transactional
    public void deletePost(Long postId, String userEmail) {
        Board board = findActiveBoard(postId);
        validateBoardOwner(board, userEmail);
        board.deleteSoftly();
    }

    /** JWT에서 확인한 로그인 사용자가 작성자인 경우에만 게시글과 이미지 목록을 수정한다. */
    @Transactional
    public BoardResponse updatePost(
            Long postId,
            String userEmail,
            String title,
            String content,
            List<String> imageUrls
    ) {
        Board board = findActiveBoard(postId);
        validateBoardOwner(board, userEmail);
        board.update(title.trim(), content.trim());

        List<BoardImage> currentImages = boardImageRepository
                .findAllByBoard_IdInOrderByIdAsc(List.of(postId));
        List<String> responseImageUrls = currentImages.stream()
                .map(BoardImage::getImageUrl)
                .toList();
        if (imageUrls != null) {
            List<String> validatedImageUrls = validateImageUrls(imageUrls);
            boardImageRepository.deleteAll(currentImages);
            if (!validatedImageUrls.isEmpty()) {
                boardImageRepository.saveAll(validatedImageUrls.stream()
                        .map(imageUrl -> new BoardImage(board, imageUrl))
                        .toList());
            }
            responseImageUrls = validatedImageUrls;
        }

        long commentCount = commentRepository.countByBoard_Id(postId);
        MarketBoard marketBoard = marketBoardRepository.findById(postId).orElse(null);
        return BoardResponse.from(board, responseImageUrls, commentCount, marketBoard);
    }

    /** 장터 글이면 가격과 거래 상태를 검증한 뒤 장터 전용 정보를 저장한다. */
    private MarketBoard saveMarketBoardIfNeeded(
            CommunityCategory category, Board board, Integer price, String tradeStatus
    ) {
        if (category != CommunityCategory.MARKET) {
            return null;
        }
        if (price == null || price < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "장터 게시글 가격은 0 이상이어야 합니다.");
        }
        String normalizedTradeStatus = tradeStatus == null ? null : tradeStatus.trim().toUpperCase(Locale.ROOT);
        if (!Set.of("SELL", "BUY").contains(normalizedTradeStatus)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "거래 종류는 SELL 또는 BUY여야 합니다.");
        }
        return marketBoardRepository.save(new MarketBoard(board, price, normalizedTradeStatus));
    }

    /** 게시글, 이미지, 댓글 수를 모아 API 응답 형태로 바꾼다. */
    private BoardResponse toResponse(Board board) {
        List<String> imageUrls = boardImageRepository
                .findAllByBoard_IdInOrderByIdAsc(List.of(board.getId()))
                .stream()
                .map(BoardImage::getImageUrl)
                .toList();
        return BoardResponse.from(
                board,
                imageUrls,
                commentRepository.countByBoard_Id(board.getId()),
                marketBoardRepository.findById(board.getId()).orElse(null)
        );
    }

    /** 삭제되지 않은 게시글을 찾는다. */
    private Board findActiveBoard(Long postId) {
        return boardRepository.findActiveById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "게시글을 찾을 수 없습니다."));
    }

    /** JWT subject인 이메일로 현재 로그인 사용자를 찾는다. */
    private User findAuthenticatedUser(String userEmail) {
        return userRepository.findByEmailAndDeletedAtIsNull(userEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인 사용자를 찾을 수 없습니다."));
    }

    /** 게시글 작성자와 로그인 사용자가 다르면 403 오류를 만든다. */
    private void validateBoardOwner(Board board, String userEmail) {
        User user = findAuthenticatedUser(userEmail);
        if (!board.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "게시글 작성자만 요청할 수 있습니다.");
        }
    }

    /** 이미지 개수와 업로드 API가 발급한 URL 형식을 검증한다. */
    private List<String> validateImageUrls(List<String> imageUrls) {
        if (imageUrls == null) {
            return List.of();
        }
        if (imageUrls.size() > MAX_IMAGE_COUNT) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "이미지는 최대 5장까지 등록할 수 있습니다.");
        }
        List<String> normalizedImageUrls = imageUrls.stream().map(String::trim).toList();
        if (normalizedImageUrls.stream().anyMatch(url -> !COMMUNITY_IMAGE_URL_PATTERN.matcher(url).matches())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "올바르지 않은 커뮤니티 이미지 주소가 포함되어 있습니다.");
        }
        return normalizedImageUrls;
    }
}
