package com.daenggo.backend.board.repository;
import com.daenggo.backend.board.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    /** 여러 게시글의 실제 댓글 개수를 한 번에 조회합니다. */
    @Query("""
            select c.board.id as boardId, count(c.id) as commentCount
            from Comment c
            where c.board.id in :boardIds
            group by c.board.id
            """)
    List<BoardCommentCount> countAllByBoardIds(@Param("boardIds") Collection<Long> boardIds);

    /** 게시글 하나의 실제 댓글 개수를 조회합니다. */
    long countByBoard_Id(Long boardId);

    interface BoardCommentCount {
        Long getBoardId();
        Long getCommentCount();
    }
}
