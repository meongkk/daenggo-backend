package com.daenggo.backend.board.repository;
import com.daenggo.backend.board.entity.BoardImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardImageRepository extends JpaRepository<BoardImage, Long> {}