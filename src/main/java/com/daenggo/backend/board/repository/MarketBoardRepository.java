package com.daenggo.backend.board.repository;
import com.daenggo.backend.board.entity.MarketBoard;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MarketBoardRepository extends JpaRepository<MarketBoard, Long> {}