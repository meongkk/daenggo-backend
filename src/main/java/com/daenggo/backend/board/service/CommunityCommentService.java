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

/** 커뮤니티 댓글의 저장, 조회, 수정, 삭제 규칙을 처리한다. */
@Service
public class CommunityCommentService {

    private final BoardRepository boardRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

    public CommunityCommentService(
            BoardRepository boardRepository,
            CommentRepository commentRepository,
            UserRepository userRepository
    ) {
        this.boardRepository = boardRepository;
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
    }

    /** 게시글의 댓글을 작성 시간순으로 조회한다. */
    @Transactional(readOnly = true)
    public List<CommentResponse> getComments(Long postId) {
        findActiveBoard(postId);
        return commentRepository.findAllWithUserByBoardId(postId).stream()
                .map(CommentResponse::from)
                .toList();
    }

    /** JWT에서 확인한 로그인 사용자를 작성자로 하여 댓글을 저장한다. */
    @Transactional
    public CommentResponse createComment(Long postId, String content, String userEmail) {
        Comment savedComment = commentRepository.save(
                new Comment(findActiveBoard(postId), findAuthenticatedUser(userEmail), content.trim())
        );
        return CommentResponse.from(savedComment);
    }

    /** JWT에서 확인한 로그인 사용자가 작성자인 경우에만 댓글을 수정한다. */
    @Transactional
    public CommentResponse updateComment(Long postId, Long commentId, String content, String userEmail) {
        findActiveBoard(postId);
        Comment comment = findComment(postId, commentId);
        validateCommentOwner(comment, userEmail);
        comment.updateContent(content.trim());
        return CommentResponse.from(commentRepository.saveAndFlush(comment));
    }

    /** JWT에서 확인한 로그인 사용자가 작성자인 경우에만 댓글을 삭제한다. */
    @Transactional
    public void deleteComment(Long postId, Long commentId, String userEmail) {
        findActiveBoard(postId);
        Comment comment = findComment(postId, commentId);
        validateCommentOwner(comment, userEmail);
        commentRepository.delete(comment);
    }

    /** 삭제되지 않은 게시글을 찾는다. */
    private Board findActiveBoard(Long postId) {
        return boardRepository.findActiveById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "게시글을 찾을 수 없습니다."));
    }

    /** 게시글에 속한 댓글을 찾는다. */
    private Comment findComment(Long postId, Long commentId) {
        return commentRepository.findByIdAndBoard_Id(commentId, postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "댓글을 찾을 수 없습니다."));
    }

    /** JWT subject인 이메일로 현재 로그인 사용자를 찾는다. */
    private User findAuthenticatedUser(String userEmail) {
        return userRepository.findByEmailAndDeletedAtIsNull(userEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인 사용자를 찾을 수 없습니다."));
    }

    /** 댓글 작성자와 로그인 사용자가 다르면 403 오류를 만든다. */
    private void validateCommentOwner(Comment comment, String userEmail) {
        User user = findAuthenticatedUser(userEmail);
        if (!comment.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "댓글 작성자만 요청할 수 있습니다.");
        }
    }
}
