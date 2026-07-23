package com.daenggo.backend.walk.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.daenggo.backend.walk.entity.WalkPhoto;
import com.daenggo.backend.walk.entity.WalkRecord;

public interface WalkPhotoRepository extends JpaRepository<WalkPhoto, Long> {

    List<WalkPhoto> findByWalkRecord(WalkRecord walkRecord);

    Optional<WalkPhoto> findByWalkPhotoIdAndWalkRecord(Long walkPhotoId, WalkRecord walkRecord);

    Optional<WalkPhoto> findFirstByWalkRecordOrderByTakenAtAsc(WalkRecord walkRecord);
    
    List<WalkPhoto> findByWalkRecord_WalkRecordId(Long walkId);

}
