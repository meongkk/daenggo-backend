package com.daenggo.backend.walk.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.daenggo.backend.walk.entity.WalkPhoto;
import com.daenggo.backend.walk.entity.WalkRecord;

public interface WalkPhotoRepository extends JpaRepository<WalkPhoto, Long> {

    List<WalkPhoto> findByWalk(WalkRecord walkRecord);

    Optional<WalkPhoto> findByWalkPhotoIdAndWalk(Long walkPhotoId, WalkRecord walkRecord);

    Optional<WalkPhoto> findFirstByWalkOrderByTakenAtAsc(WalkRecord walkRecord);

}