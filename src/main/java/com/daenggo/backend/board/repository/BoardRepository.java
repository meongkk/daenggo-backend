package com.daenggo.backend.board.repository;

import com.daenggo.backend.board.entity.Board;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * 게시글 엔티티의 데이터 접근을 담당하는 저장소.
 */
public interface BoardRepository extends JpaRepository<Board, Long> {

    /**
     * 카테고리가 일치하고 삭제되지 않은 게시글을 최신순으로 조회한다.
     *
     * @param type 조회할 게시글 카테고리 값
     * @return 작성자 정보가 포함된 게시글 목록
     */
    @EntityGraph(attributePaths = "user")
    List<Board> findAllByTypeAndDeletedAtIsNullOrderByCreatedAtDesc(String type);

    /**
     * 삭제되지 않은 게시글을 식별자로 조회한다.
     *
     * @param id 조회할 게시글 식별자
     * @return 삭제되지 않은 게시글
     */
    @Query("""
            select b
            from Board b
            where b.id = :id
              and b.deletedAt is null
            """)
    Optional<Board> findActiveById(@Param("id") Long id);
}
