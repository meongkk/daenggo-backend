package com.daenggo.backend.board.service;

import com.daenggo.backend.board.dto.BoardResponse;
import com.daenggo.backend.board.entity.Board;
import com.daenggo.backend.board.repository.BoardRepository;
import com.daenggo.backend.user.entity.User;
import jakarta.persistence.EntityManager;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * 커뮤니티 게시글 등록 비즈니스 로직을 처리하는 서비스.
 */
@Service
public class CommunityPostService {

    /** 커뮤니티 게시글을 구분하는 게시판 종류 값. */
    private static final String COMMUNITY_TYPE = "COMMUNITY";

    /** 게시글 저장소. */
    private final BoardRepository boardRepository;

    /** 작성자 엔티티 조회에 사용하는 엔티티 매니저. */
    private final EntityManager entityManager;

    /**
     * 커뮤니티 게시글 서비스를 생성한다.
     *
     * @param boardRepository 게시글 저장소
     * @param entityManager 작성자 조회에 사용할 엔티티 매니저
     */
    public CommunityPostService(BoardRepository boardRepository, EntityManager entityManager) {
        this.boardRepository = boardRepository;
        this.entityManager = entityManager;
    }

    /**
     * 작성자를 확인하고 새로운 커뮤니티 게시글을 저장한다.
     *
     * @param title 게시글 제목
     * @param content 게시글 내용
     * @param userId 작성자 식별자
     * @return 저장된 게시글 응답 데이터
     * @throws ResponseStatusException 작성자를 찾을 수 없는 경우
     */
    @Transactional
    public BoardResponse createPost(String title, String content, Long userId) {
        User user = entityManager.find(User.class, userId);

        if (user == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "작성자를 찾을 수 없습니다. userId=" + userId
            );
        }

        Board board = new Board(COMMUNITY_TYPE, title, content, user);
        Board savedBoard = boardRepository.save(board);

        return BoardResponse.from(savedBoard);
    }
}
