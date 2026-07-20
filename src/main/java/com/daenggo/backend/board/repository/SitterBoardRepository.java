package com.daenggo.backend.board.repository;
import com.daenggo.backend.board.entity.SitterBoard;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SitterBoardRepository extends JpaRepository<SitterBoard, Long> {}