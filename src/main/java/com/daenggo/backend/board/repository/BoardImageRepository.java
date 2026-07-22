package com.daenggo.backend.board.repository;

import com.daenggo.backend.board.entity.BoardImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

/**
 * 게시글 이미지 엔티티의 데이터 접근을 담당하는 저장소.
 */
public interface BoardImageRepository extends JpaRepository<BoardImage, Long> {

    /**
     * 여러 게시글에 연결된 이미지를 등록 순서대로 조회한다.
     *
     * @param boardIds 조회할 게시글 식별자 목록
     * @return 게시글에 연결된 이미지 목록
     */
    List<BoardImage> findAllByBoard_IdInOrderByIdAsc(Collection<Long> boardIds);
}
