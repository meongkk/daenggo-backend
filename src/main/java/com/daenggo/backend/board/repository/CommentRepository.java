package com.daenggo.backend.board.repository;
import com.daenggo.backend.board.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {}