package com.daenggo.backend.board.repository;
import com.daenggo.backend.board.entity.Comment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    /**
     * 게시글에 연결된 댓글과 작성자를 등록 순서대로 조회한다.
     *
     * @param boardId 조회할 게시글 식별자
     * @return 작성자 정보가 포함된 댓글 목록
     */
    @EntityGraph(attributePaths = "user")
    @Query("""
            select c
            from Comment c
            where c.board.id = :boardId
            order by c.createdAt asc, c.id asc
            """)
    List<Comment> findAllWithUserByBoardId(@Param("boardId") Long boardId);

    /** 여러 게시글의 실제 댓글 개수를 한 번에 조회한다. */
    @Query("""
            select c.board.id as boardId, count(c.id) as commentCount
            from Comment c
            where c.board.id in :boardIds
            group by c.board.id
            """)
    List<BoardCommentCount> countAllByBoardIds(@Param("boardIds") Collection<Long> boardIds);

    /** 게시글 하나의 실제 댓글 개수를 조회한다. */
    long countByBoard_Id(Long boardId);

    /** 댓글 ID와 게시글 ID가 모두 일치하는 댓글을 작성자 정보와 함께 조회한다. */
    @EntityGraph(attributePaths = "user")
    Optional<Comment> findByIdAndBoard_Id(Long commentId, Long boardId);

    interface BoardCommentCount {
        Long getBoardId();
        Long getCommentCount();
    }
}
