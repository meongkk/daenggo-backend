package com.daenggo.backend.board.service;

import com.daenggo.backend.board.dto.CommentResponse;
import com.daenggo.backend.board.entity.Board;
import com.daenggo.backend.board.entity.Comment;
import com.daenggo.backend.board.repository.BoardRepository;
import com.daenggo.backend.board.repository.CommentRepository;
import com.daenggo.backend.user.entity.User;
import com.daenggo.backend.user.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * 커뮤니티 댓글 등록과 목록 조회 비즈니스 로직을 처리하는 서비스.
 */
@Service
public class CommunityCommentService {

    /** 게시글 저장소. */
    private final BoardRepository boardRepository;

    /** 댓글 저장소. */
    private final CommentRepository commentRepository;

    /** 사용자 저장소. */
    private final UserRepository userRepository;

    /**
     * 커뮤니티 댓글 서비스를 생성한다.
     *
     * @param boardRepository 게시글 저장소
     * @param commentRepository 댓글 저장소
     * @param userRepository 사용자 저장소
     */
    public CommunityCommentService(
            BoardRepository boardRepository,
            CommentRepository commentRepository,
            UserRepository userRepository
    ) {
        this.boardRepository = boardRepository;
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
    }

    /**
     * 삭제되지 않은 게시글의 댓글을 등록 순서대로 조회한다.
     *
     * @param postId 댓글을 조회할 게시글 식별자
     * @return 댓글 응답 목록
     * @throws ResponseStatusException 게시글이 없거나 삭제된 경우
     */
    @Transactional(readOnly = true)
    public List<CommentResponse> getComments(Long postId) {
        findActiveBoard(postId);

        return commentRepository.findAllWithUserByBoardId(postId)
                .stream()
                .map(CommentResponse::from)
                .toList();
    }

    /**
     * 게시글과 작성자를 확인한 뒤 새로운 댓글을 저장한다.
     *
     * @param postId 댓글을 등록할 게시글 식별자
     * @param content 댓글 내용
     * @param userId 댓글 작성자 식별자
     * @return 저장된 댓글 응답
     * @throws ResponseStatusException 게시글 또는 작성자를 찾을 수 없는 경우
     */
    @Transactional
    public CommentResponse createComment(Long postId, String content, Long userId) {
        Board board = findActiveBoard(postId);
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "댓글 작성자를 찾을 수 없습니다. userId=" + userId
                ));

        Comment comment = new Comment(board, user, content.trim());
        Comment savedComment = commentRepository.save(comment);

        return CommentResponse.from(savedComment);
    }

    /**
     * 댓글 작성자가 맞는지 확인한 뒤 댓글 내용을 수정한다.
     */
    @Transactional
    public CommentResponse updateComment(
            Long postId,
            Long commentId,
            String content,
            Long userId
    ) {
        findActiveBoard(postId);
        Comment comment = findComment(postId, commentId);
        validateCommentOwner(comment, userId);

        comment.updateContent(content.trim());
        Comment updatedComment = commentRepository.saveAndFlush(comment);

        return CommentResponse.from(updatedComment);
    }

    /**
     * 댓글 작성자가 맞는지 확인한 뒤 댓글을 삭제한다.
     */
    @Transactional
    public void deleteComment(Long postId, Long commentId, Long userId) {
        findActiveBoard(postId);
        Comment comment = findComment(postId, commentId);
        validateCommentOwner(comment, userId);
        commentRepository.delete(comment);
    }

    /** 게시글에 속한 댓글을 찾고, 없으면 404 오류를 보낸다. */
    private Comment findComment(Long postId, Long commentId) {
        return commentRepository.findByIdAndBoard_Id(commentId, postId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "댓글을 찾을 수 없습니다. commentId=" + commentId
                ));
    }

    /** 요청한 사용자와 실제 댓글 작성자가 다르면 403 오류를 보낸다. */
    private void validateCommentOwner(Comment comment, Long userId) {
        if (!comment.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "본인이 작성한 댓글만 수정하거나 삭제할 수 있습니다."
            );
        }
    }

    /**
     * 삭제되지 않은 게시글을 조회한다.
     *
     * @param postId 조회할 게시글 식별자
     * @return 조회된 게시글 엔티티
     * @throws ResponseStatusException 게시글이 없거나 삭제된 경우
     */
    private Board findActiveBoard(Long postId) {
        return boardRepository.findActiveById(postId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "게시글을 찾을 수 없습니다. postId=" + postId
                ));
    }
}
