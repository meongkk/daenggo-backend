package com.daenggo.backend.board.repository;

import com.daenggo.backend.board.entity.Board;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardRepository  extends JpaRepository<Board, Long> {

    
}
